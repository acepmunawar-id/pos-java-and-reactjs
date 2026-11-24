package com.pos.service;

import com.pos.dto.CustomerDebtResponse;
import com.pos.dto.CustomerDebtResponse.DebtSaleRow;
import com.pos.dto.DebtPaymentRequest;
import com.pos.dto.DebtPaymentResponse;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.model.Customer;
import com.pos.model.DebtPayment;
import com.pos.model.PaymentStatus;
import com.pos.model.Sale;
import com.pos.model.User;
import com.pos.repository.DebtPaymentRepository;
import com.pos.repository.SaleRepository;
import com.pos.repository.UserRepository;
import com.pos.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DebtServiceImpl implements DebtService {

    private final SaleRepository saleRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDebtResponse> listAllDebts() {
        List<Sale> unpaidSales = saleRepository.findByPaymentStatus(PaymentStatus.UNPAID);

        // group by customer
        Map<UUID, CustomerDebtResponse> map = new LinkedHashMap<>();
        for (Sale sale : unpaidSales) {
            Customer customer = sale.getCustomer();
            if (customer == null) continue;

            BigDecimal totalPaid = debtPaymentRepository.sumBySaleId(sale.getId());
            BigDecimal remaining = sale.getTotal().subtract(totalPaid);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) continue; // sudah lunas tapi status belum diupdate

            CustomerDebtResponse dto = map.computeIfAbsent(customer.getId(), id ->
                CustomerDebtResponse.builder()
                    .customerId(customer.getId())
                    .customerName(customer.getName())
                    .customerPhone(customer.getPhone())
                    .totalDebt(BigDecimal.ZERO)
                    .sales(new ArrayList<>())
                    .build()
            );

            dto.getSales().add(DebtSaleRow.builder()
                .saleId(sale.getId())
                .invoiceNumber(sale.getInvoiceNumber())
                .saleDate(sale.getSaleDate())
                .total(sale.getTotal())
                .totalPaid(totalPaid)
                .remaining(remaining)
                .build());

            dto.setTotalDebt(dto.getTotalDebt().add(remaining));
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public DebtPaymentResponse pay(DebtPaymentRequest request) {
        Sale sale = saleRepository.findById(request.getSaleId())
            .orElseThrow(() -> ResourceNotFoundException.of("Transaksi", request.getSaleId()));

        if (sale.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Transaksi ini sudah lunas");
        }

        BigDecimal totalPaid = debtPaymentRepository.sumBySaleId(sale.getId());
        BigDecimal remaining = sale.getTotal().subtract(totalPaid);

        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException(
                "Jumlah bayar (" + request.getAmount() + ") melebihi sisa hutang (" + remaining + ")");
        }

        User cashier = currentUser();
        DebtPayment payment = DebtPayment.builder()
            .sale(sale)
            .amount(request.getAmount())
            .notes(request.getNotes())
            .cashier(cashier)
            .build();
        payment = debtPaymentRepository.save(payment);

        // Update status sale jika sudah lunas
        BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
        if (newTotalPaid.compareTo(sale.getTotal()) >= 0) {
            sale.setPaymentStatus(PaymentStatus.PAID);
            sale.setAmountPaid(newTotalPaid);
            saleRepository.save(sale);
        }

        return DebtPaymentResponse.builder()
            .id(payment.getId())
            .saleId(sale.getId())
            .invoiceNumber(sale.getInvoiceNumber())
            .amount(payment.getAmount())
            .notes(payment.getNotes())
            .cashierName(cashier.getFullName())
            .paidAt(payment.getCreatedAt())
            .build();
    }

    private User currentUser() {
        String username = SecurityUtils.currentUsername();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' tidak ditemukan"));
    }
}
