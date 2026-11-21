package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.PageResponse;
import com.pos.dto.UnitRequest;
import com.pos.dto.UnitResponse;
import com.pos.service.UnitService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UnitResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(unitService.findAll(search, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(unitService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UnitResponse>> create(@Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Satuan berhasil dibuat", unitService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<UnitResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Satuan berhasil diperbarui", unitService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        unitService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Satuan berhasil dihapus", null));
    }
}
