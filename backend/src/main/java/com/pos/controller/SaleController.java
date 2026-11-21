package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.PageResponse;
import com.pos.dto.SaleRequest;
import com.pos.dto.SaleResponse;
import com.pos.service.SaleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Transaksi kasir (POS). Semua role yang login boleh membuat & melihat transaksi;
 * kasir (role USER) hanya melihat transaksinya sendiri (ditegakkan di service).
 */
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<ApiResponse<SaleResponse>> create(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transaksi berhasil disimpan", saleService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SaleResponse>>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(saleService.findAll(from, to, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.findById(id)));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<SaleResponse>> voidSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Transaksi berhasil dibatalkan", saleService.voidSale(id)));
    }
}
