package com.pos.partner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Shared shape for Customer and Supplier create/update payloads - both have identical fields. */
@Getter
@Setter
public class PartnerRequest {

    @NotBlank(message = "Nama wajib diisi")
    private String name;

    private String phone;

    @Email(message = "Format email tidak valid")
    private String email;

    private String address;
}
