package com.pos.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.report.service.ReportService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(ReportController.class)
class ReportControllerTest extends AbstractControllerTest {

    @MockBean
    private ReportService reportService;

    @Test
    void salesSummary_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/reports/sales/summary.csv")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void salesSummary_returns403_forCashierRole() throws Exception {
        mockMvc.perform(get("/api/reports/sales/summary.csv")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void salesSummary_returns200WithCsvAttachment_forAdminRole() throws Exception {
        when(reportService.generateSalesSummaryCsv(any(), any()))
                .thenReturn("No Invoice,Total\nINV-001,6000\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/reports/sales/summary.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"laporan-penjualan.csv\""));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void salesDetail_returns200_forAdminRole() throws Exception {
        when(reportService.generateSalesDetailCsv(any(), any()))
                .thenReturn("No Invoice,Produk\nINV-001,Kopi\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/reports/sales/detail.csv")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void productStock_returns200_forSuperAdminRole() throws Exception {
        when(reportService.generateProductStockCsv())
                .thenReturn("SKU,Stok\nSKU-001,10\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/reports/products/stock.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"laporan-stok-produk.csv\""));
    }
}
