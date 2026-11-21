package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.PageResponse;
import com.pos.dto.DiscountRequest;
import com.pos.dto.DiscountResponse;
import com.pos.service.DiscountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DiscountResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(discountService.findAll(search, pageable))));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> findAllActive() {
        return ResponseEntity.ok(ApiResponse.success(discountService.findAllActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(discountService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DiscountResponse>> create(@Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Diskon berhasil dibuat", discountService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DiscountResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Diskon berhasil diperbarui", discountService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        discountService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Diskon berhasil dihapus", null));
    }
}
