package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.AppSettingRequest;
import com.pos.dto.AppSettingResponse;
import com.pos.service.AppSettingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class AppSettingController {

    private final AppSettingService settingService;

    /** Semua role bisa baca settings (nama toko, dll dipakai di struk). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppSettingResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(settingService.findAll()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<AppSettingResponse>> findByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(settingService.findByKey(key)));
    }

    /** Hanya admin yang boleh mengubah setting. */
    @PutMapping("/{key}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<AppSettingResponse>> update(
            @PathVariable String key,
            @Valid @RequestBody AppSettingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Setting berhasil diperbarui", settingService.update(key, request)));
    }
}
