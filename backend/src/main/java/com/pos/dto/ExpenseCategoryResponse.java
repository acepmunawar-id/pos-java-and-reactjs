package com.pos.dto;

import java.util.UUID;

public record ExpenseCategoryResponse(
        UUID id,
        String name,
        String description
) {}
