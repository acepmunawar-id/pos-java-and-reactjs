package com.pos.controller;

import com.pos.dto.ApiResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Path UPLOAD_DIR = Paths.get("uploads/products");
    private static final Set<String> ALLOWED = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    @PostMapping("/product-image")
    public ResponseEntity<ApiResponse<String>> uploadProductImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        String original  = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext       = original.contains(".")
                ? original.substring(original.lastIndexOf(".")).toLowerCase()
                : ".jpg";

        if (!ALLOWED.contains(ext)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Format file tidak didukung. Gunakan JPG, PNG, atau WEBP."));
        }

        Files.createDirectories(UPLOAD_DIR);
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok(ApiResponse.success("/uploads/products/" + filename));
    }
}
