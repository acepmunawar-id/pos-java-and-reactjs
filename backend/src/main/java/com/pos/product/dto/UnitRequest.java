package com.pos.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitRequest {
    @NotBlank(message = "Nama satuan wajib diisi")
    private String name;

    @NotBlank(message = "Simbol satuan wajib diisi")
    private String symbol;
}
