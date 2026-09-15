package com.pos.pricing.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.pricing.dto.TaxRequest;
import com.pos.pricing.dto.TaxResponse;
import com.pos.pricing.service.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/taxes")
@RequiredArgsConstructor
@Tag(name = "Master Data - Pajak")
public class TaxController {

    private final TaxService taxService;

    @GetMapping
    @Operation(summary = "Daftar pajak (paginated, bisa dicari)")
    public ResponseEntity<ApiResponse<PageResponse<TaxResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(taxService.findAll(search, pageable))));
    }

    @GetMapping("/active")
    @Operation(summary = "Daftar pajak aktif (dipakai dropdown di halaman kasir)")
    public ResponseEntity<ApiResponse<List<TaxResponse>>> findAllActive() {
        return ResponseEntity.ok(ApiResponse.success(taxService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taxService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TaxResponse>> create(@Valid @RequestBody TaxRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pajak berhasil dibuat", taxService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<TaxResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody TaxRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pajak berhasil diperbarui", taxService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        taxService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Pajak berhasil dihapus", null));
    }
}
