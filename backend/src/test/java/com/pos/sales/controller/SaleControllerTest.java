package com.pos.sales.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.BadRequestException;
import com.pos.common.exception.InsufficientStockException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.sales.dto.SaleResponse;
import com.pos.sales.entity.PaymentMethod;
import com.pos.sales.entity.SaleStatus;
import com.pos.sales.service.SaleService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(SaleController.class)
class SaleControllerTest extends AbstractControllerTest {

    @MockBean
    private SaleService saleService;

    private String validPayload() {
        return """
                {
                  "paymentMethod": "CASH",
                  "amountPaid": 10000,
                  "items": [ { "productId": "%s", "quantity": 2 } ]
                }
                """.formatted(UUID.randomUUID());
    }

    private SaleResponse sampleSale() {
        return SaleResponse.builder()
                .id(UUID.randomUUID())
                .invoiceNumber("INV-20260101-000001")
                .subtotal(new BigDecimal("6000"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .total(new BigDecimal("6000"))
                .paymentMethod(PaymentMethod.CASH)
                .amountPaid(new BigDecimal("10000"))
                .changeAmount(new BigDecimal("4000"))
                .status(SaleStatus.COMPLETED)
                .items(List.of())
                .build();
    }

    @Test
    void create_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns200_forCashierRole() throws Exception {
        when(saleService.create(any())).thenReturn(sampleSale());

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns400_whenCartEmpty() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"paymentMethod\": \"CASH\", \"amountPaid\": 1000, \"items\": [] }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns409_whenStockInsufficient() throws Exception {
        when(saleService.create(any())).thenThrow(new InsufficientStockException("Kopi Sachet", 1, 2));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns400_whenAmountPaidTooLow() throws Exception {
        when(saleService.create(any())).thenThrow(new BadRequestException("Jumlah bayar kurang dari total tagihan"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forCashierRole() throws Exception {
        when(saleService.findAll(any(), any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/sales")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findById_returns403_whenViewingAnotherCashiersSale() throws Exception {
        UUID id = UUID.randomUUID();
        when(saleService.findById(eq(id))).thenThrow(new AccessDeniedException("Anda hanya dapat melihat transaksi milik sendiri"));

        mockMvc.perform(get("/api/sales/" + id)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findById_returns404_whenSaleMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(saleService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Transaksi", id));

        mockMvc.perform(get("/api/sales/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void voidSale_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/sales/" + UUID.randomUUID() + "/void")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void voidSale_returns200_forAdminRole() throws Exception {
        SaleResponse voided = sampleSale();
        voided.setStatus(SaleStatus.VOID);
        when(saleService.voidSale(any())).thenReturn(voided);

        mockMvc.perform(post("/api/sales/" + UUID.randomUUID() + "/void")).andExpect(status().isOk());
    }
}
