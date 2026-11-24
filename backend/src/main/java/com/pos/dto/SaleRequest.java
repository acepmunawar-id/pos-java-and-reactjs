package com.pos.dto;

import com.pos.model.PaymentMethod;
import com.pos.model.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleRequest {

    private UUID customerId;

    private UUID taxId;

    private UUID discountId;

    /** Null saat paymentStatus = UNPAID (hutang). */
    private PaymentMethod paymentMethod;

    /** Null atau 0 saat paymentStatus = UNPAID (hutang). */
    @DecimalMin(value = "0", message = "Jumlah bayar tidak boleh negatif")
    private BigDecimal amountPaid;

    /** Default PAID; kirim UNPAID untuk transaksi hutang. */
    private PaymentStatus paymentStatus = PaymentStatus.PAID;

    @NotEmpty(message = "Keranjang tidak boleh kosong")
    @Valid
    private List<SaleItemRequest> items;
}
