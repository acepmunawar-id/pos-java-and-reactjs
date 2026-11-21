package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.ReceiptView;
import com.pos.service.ReceiptService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales/{id}/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    public ResponseEntity<ApiResponse<ReceiptView>> view(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.buildView(id)));
    }

    @GetMapping(value = "/escpos", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> escPos(@PathVariable UUID id) {
        byte[] bytes = receiptService.buildEscPos(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=receipt.bin")
                .body(bytes);
    }
}
