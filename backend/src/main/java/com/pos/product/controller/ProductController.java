package com.pos.product.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.product.dto.ProductRequest;
import com.pos.product.dto.ProductResponse;
import com.pos.product.service.ProductService;
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

/**
 * Master data produk. GET terbuka untuk semua role login (dipakai halaman kasir
 * untuk mencari produk); perubahan data dibatasi SUPER_ADMIN & ADMIN.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Master Data - Produk")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Daftar produk (paginated, cari berdasarkan nama/SKU/barcode)")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(productService.findAll(search, pageable))));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Daftar produk dengan stok di bawah/sama dengan stok minimum")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> findLowStock() {
        return ResponseEntity.ok(ApiResponse.success(productService.findLowStock()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.findById(id)));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Cari produk berdasarkan barcode (untuk scan di halaman kasir)")
    public ResponseEntity<ApiResponse<ProductResponse>> findByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(ApiResponse.success(productService.findByBarcode(barcode)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Produk berhasil dibuat", productService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Produk berhasil diperbarui", productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Produk berhasil dihapus", null));
    }
}
