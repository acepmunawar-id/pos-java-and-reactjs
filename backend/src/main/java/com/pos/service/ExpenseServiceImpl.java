package com.pos.service;

import com.pos.dto.ExpenseCategoryResponse;
import com.pos.dto.ExpenseRequest;
import com.pos.dto.ExpenseResponse;
import com.pos.dto.FinancialSummaryResponse;
import com.pos.exception.ResourceNotFoundException;
import com.pos.mapper.ExpenseMapper;
import com.pos.model.Expense;
import com.pos.model.ExpenseCategory;
import com.pos.model.Supplier;
import com.pos.repository.AppSettingRepository;
import com.pos.repository.ExpenseCategoryRepository;
import com.pos.repository.ExpenseRepository;
import com.pos.repository.SaleRepository;
import com.pos.repository.SupplierRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository           expenseRepository;
    private final ExpenseCategoryRepository   categoryRepository;
    private final SupplierRepository          supplierRepository;
    private final SaleRepository              saleRepository;
    private final AppSettingRepository        settingRepository;
    private final ExpenseMapper               expenseMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> findAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(expenseMapper::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> findAll(LocalDate from, LocalDate to, UUID categoryId, Pageable pageable) {
        return expenseRepository.findByFilter(from, to, categoryId, pageable)
                .map(expenseMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse findById(UUID id) {
        return expenseMapper.toResponse(getOrThrow(id));
    }

    @Override
    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = expenseMapper.toEntity(request);
        expense.setCategory(getCategoryOrThrow(request.categoryId()));
        if (request.supplierId() != null) {
            expense.setSupplier(getSupplierOrThrow(request.supplierId()));
        }
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    public ExpenseResponse update(UUID id, ExpenseRequest request) {
        Expense expense = getOrThrow(id);
        expenseMapper.updateEntity(request, expense);
        expense.setCategory(getCategoryOrThrow(request.categoryId()));
        expense.setSupplier(request.supplierId() != null ? getSupplierOrThrow(request.supplierId()) : null);
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    public void delete(UUID id) {
        expenseRepository.delete(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialSummaryResponse getSummary(LocalDate from, LocalDate to) {
        Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant toInstant   = to   != null ? to.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) : null;

        BigDecimal totalMasuk  = sumSales(fromInstant, toInstant);
        BigDecimal totalKeluar = sumExpenses(from, to);
        BigDecimal selisih     = totalMasuk.subtract(totalKeluar);

        // Saldo kumulatif: modal_awal + semua masuk - semua keluar (all-time s/d to)
        BigDecimal modalAwal = settingRepository.findById("modal_awal")
                .map(s -> { try { return new BigDecimal(s.getValue()); } catch (Exception e) { return BigDecimal.ZERO; } })
                .orElse(BigDecimal.ZERO);
        BigDecimal totalMasukAll  = toInstant != null ? saleRepository.sumCompletedTo(toInstant)   : saleRepository.sumCompletedAll();
        BigDecimal totalKeluarAll = to        != null ? expenseRepository.sumAmountTo(to)           : expenseRepository.sumAmountAll();
        BigDecimal saldo          = modalAwal.add(totalMasukAll).subtract(totalKeluarAll);

        return new FinancialSummaryResponse(totalMasuk, totalKeluar, selisih, saldo);
    }

    private BigDecimal sumSales(Instant from, Instant to) {
        if (from != null && to != null) return saleRepository.sumCompletedBetween(from, to);
        if (to   != null)               return saleRepository.sumCompletedTo(to);
        return saleRepository.sumCompletedAll();
    }

    private BigDecimal sumExpenses(LocalDate from, LocalDate to) {
        if (from != null && to != null) return expenseRepository.sumAmountBetween(from, to);
        if (to   != null)               return expenseRepository.sumAmountTo(to);
        return expenseRepository.sumAmountAll();
    }

    private Expense getOrThrow(UUID id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Expense", id));
    }

    private ExpenseCategory getCategoryOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ExpenseCategory", id));
    }

    private Supplier getSupplierOrThrow(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }
}
