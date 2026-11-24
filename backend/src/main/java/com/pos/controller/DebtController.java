package com.pos.controller;

import com.pos.dto.ApiResponse;
import com.pos.dto.CustomerDebtResponse;
import com.pos.dto.DebtPaymentRequest;
import com.pos.dto.DebtPaymentResponse;
import com.pos.service.DebtService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDebtResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(debtService.listAllDebts()));
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<DebtPaymentResponse>> pay(@Valid @RequestBody DebtPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(debtService.pay(request)));
    }
}
