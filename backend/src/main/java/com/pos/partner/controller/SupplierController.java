package com.pos.partner.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Master Data - Supplier")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Daftar supplier (paginated, bisa dicari)")
    public ResponseEntity<ApiResponse<PageResponse<PartnerResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(supplierService.findAll(search, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PartnerResponse>> create(@Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier berhasil dibuat", supplierService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PartnerResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Supplier berhasil diperbarui", supplierService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier berhasil dihapus", null));
    }
}
