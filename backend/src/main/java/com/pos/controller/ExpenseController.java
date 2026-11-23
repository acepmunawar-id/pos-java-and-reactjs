package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.ExpenseCategoryResponse;
import com.pos.dto.ExpenseRequest;
import com.pos.dto.ExpenseResponse;
import com.pos.dto.FinancialSummaryResponse;
import com.pos.dto.PageResponse;
import com.pos.service.ExpenseService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class ExpenseController {

    private final ExpenseService expenseService;

    /** Daftar kategori pengeluaran */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ExpenseCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(expenseService.findAllCategories()));
    }

    /** Ringkasan keuangan: total masuk, keluar, selisih, saldo */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FinancialSummaryResponse>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getSummary(from, to)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.from(expenseService.findAll(from, to, categoryId, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pengeluaran berhasil dicatat", expenseService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pengeluaran berhasil diperbarui", expenseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        expenseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Pengeluaran berhasil dihapus", null));
    }
}
