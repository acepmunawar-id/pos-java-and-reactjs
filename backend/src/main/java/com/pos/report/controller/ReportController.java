package com.pos.report.controller;

import com.pos.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Ekspor laporan dalam format CSV - hanya SUPER_ADMIN & ADMIN yang berhak. */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Laporan", description = "Ekspor laporan penjualan & stok dalam format CSV")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales/summary.csv")
    @Operation(summary = "Ekspor CSV ringkasan transaksi per invoice dalam rentang tanggal")
    public ResponseEntity<byte[]> salesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return csvResponse(reportService.generateSalesSummaryCsv(from, to), "laporan-penjualan.csv");
    }

    @GetMapping("/sales/detail.csv")
    @Operation(summary = "Ekspor CSV rincian item terjual dalam rentang tanggal")
    public ResponseEntity<byte[]> salesDetail(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return csvResponse(reportService.generateSalesDetailCsv(from, to), "laporan-item-terjual.csv");
    }

    @GetMapping("/products/stock.csv")
    @Operation(summary = "Ekspor CSV laporan stok produk saat ini")
    public ResponseEntity<byte[]> productStock() {
        return csvResponse(reportService.generateProductStockCsv(), "laporan-stok-produk.csv");
    }

    private ResponseEntity<byte[]> csvResponse(byte[] body, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
