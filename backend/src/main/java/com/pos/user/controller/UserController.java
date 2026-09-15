package com.pos.user.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.user.dto.UserRequest;
import com.pos.user.dto.UserResponse;
import com.pos.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Manajemen akun & role - hanya SUPER_ADMIN yang berhak mengakses modul ini. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Manajemen User", description = "CRUD akun pengguna & role (khusus SUPER_ADMIN)")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Daftar user (paginated, bisa dicari berdasarkan nama)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAll(
            @RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(userService.findAll(search, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User berhasil dibuat", userService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User berhasil diperbarui", userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User berhasil dihapus", null));
    }
}
