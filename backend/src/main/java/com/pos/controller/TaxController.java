package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.PageResponse;
import com.pos.dto.TaxRequest;
import com.pos.dto.TaxResponse;
import com.pos.service.TaxService;
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
public class TaxController {

    private final TaxService taxService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TaxResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(taxService.findAll(search, pageable))));
    }

    @GetMapping("/active")
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
