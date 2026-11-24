package com.pos.service;

import com.pos.dto.ExpenseCategoryResponse;
import com.pos.dto.ExpenseRequest;
import com.pos.dto.ExpenseResponse;
import com.pos.dto.FinancialSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

    List<ExpenseCategoryResponse> findAllCategories();

    Page<ExpenseResponse> findAll(LocalDate from, LocalDate to, UUID categoryId, Pageable pageable);

    ExpenseResponse findById(UUID id);

    ExpenseResponse create(ExpenseRequest request);

    ExpenseResponse update(UUID id, ExpenseRequest request);

    void delete(UUID id);

    FinancialSummaryResponse getSummary(LocalDate from, LocalDate to);
}
