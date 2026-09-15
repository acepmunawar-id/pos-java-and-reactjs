package com.pos.receipt.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.receipt.dto.ReceiptView;
import com.pos.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Struk / Bill", description = "Data struk untuk cetak (printable HTML fallback dan ESC/POS thermal printer)")
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    @Operation(summary = "Data struk terstruktur (dipakai frontend untuk render HTML 80mm / fallback window.print)")
    public ResponseEntity<ApiResponse<ReceiptView>> view(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(receiptService.buildView(id)));
    }

    @GetMapping(value = "/escpos", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Byte ESC/POS mentah untuk dikirim langsung ke printer thermal (mis. via Web Serial API)")
    public ResponseEntity<byte[]> escPos(@PathVariable UUID id) {
        byte[] bytes = receiptService.buildEscPos(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=receipt.bin")
                .body(bytes);
    }
}
