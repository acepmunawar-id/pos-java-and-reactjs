package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.PageResponse;
import com.pos.dto.PartnerRequest;
import com.pos.dto.PartnerResponse;
import com.pos.service.CustomerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PartnerResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(customerService.findAll(search, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PartnerResponse>> create(@Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pelanggan berhasil dibuat", customerService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PartnerResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody PartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Pelanggan berhasil diperbarui", customerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Pelanggan berhasil dihapus", null));
    }
}
