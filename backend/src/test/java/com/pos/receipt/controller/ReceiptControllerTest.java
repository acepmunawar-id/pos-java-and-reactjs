package com.pos.receipt.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.receipt.dto.ReceiptView;
import com.pos.receipt.service.ReceiptService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(ReceiptController.class)
class ReceiptControllerTest extends AbstractControllerTest {

    @MockBean
    private ReceiptService receiptService;

    private final UUID saleId = UUID.randomUUID();

    @Test
    void view_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/sales/" + saleId + "/receipt")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void view_returns200WithStructuredReceipt_forAnyAuthenticatedRole() throws Exception {
        when(receiptService.buildView(saleId)).thenReturn(ReceiptView.builder()
                .storeName("Toko Sederhana")
                .invoiceNumber("INV-20260101-000001")
                .saleDate(Instant.now())
                .items(List.of())
                .subtotal(new BigDecimal("6000"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .total(new BigDecimal("6000"))
                .amountPaid(new BigDecimal("10000"))
                .changeAmount(new BigDecimal("4000"))
                .build());

        mockMvc.perform(get("/api/sales/" + saleId + "/receipt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-20260101-000001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void escPos_returns200WithOctetStreamBytes() throws Exception {
        byte[] bytes = {0x1B, 0x40, 'H', 'i'};
        when(receiptService.buildEscPos(saleId)).thenReturn(bytes);

        mockMvc.perform(get("/api/sales/" + saleId + "/receipt/escpos"))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertArrayEquals(
                        bytes, result.getResponse().getContentAsByteArray()));
    }
}
