package com.pos.sales.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.sales.dto.SaleRequest;
import com.pos.sales.dto.SaleResponse;
import com.pos.sales.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transaksi", description = "Transaksi kasir (point of sale)")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Buat transaksi baru (checkout kasir)")
    public ResponseEntity<ApiResponse<SaleResponse>> create(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transaksi berhasil disimpan", saleService.create(request)));
    }

    @GetMapping
    @Operation(summary = "Daftar transaksi (paginated, filter rentang tanggal)")
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
    @Operation(summary = "Batalkan transaksi & kembalikan stok produk terkait")
    public ResponseEntity<ApiResponse<SaleResponse>> voidSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Transaksi berhasil dibatalkan", saleService.voidSale(id)));
    }
}
