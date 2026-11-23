package com.pos.dto;

import jakarta.validation.constraints.NotNull;

public record AppSettingRequest(
        @NotNull(message = "Value wajib diisi")
        String value
) {}
