package com.pos.dto;

public record AppSettingResponse(
        String key,
        String value,
        String description
) {}
