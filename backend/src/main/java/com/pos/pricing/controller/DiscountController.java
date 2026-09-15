package com.pos.pricing.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.pricing.dto.DiscountRequest;
import com.pos.pricing.dto.DiscountResponse;
import com.pos.pricing.service.DiscountService;
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
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Tag(name = "Master Data - Diskon")
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    @Operation(summary = "Daftar diskon (paginated, bisa dicari)")
    public ResponseEntity<ApiResponse<PageResponse<DiscountResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(discountService.findAll(search, pageable))));
    }

    @GetMapping("/active")
    @Operation(summary = "Daftar diskon aktif (dipakai dropdown di halaman kasir)")
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
