package com.pos.dto;

import com.pos.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Metode pembayaran wajib dipilih")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Jumlah bayar wajib diisi")
    @DecimalMin(value = "0", message = "Jumlah bayar tidak boleh negatif")
    private BigDecimal amountPaid;

    @NotEmpty(message = "Keranjang tidak boleh kosong")
    @Valid
    private List<SaleItemRequest> items;
}
