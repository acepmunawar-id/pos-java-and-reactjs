package com.pos.service;

import com.opencsv.CSVWriter;
import com.pos.model.Expense;
import com.pos.model.Product;
import com.pos.model.Sale;
import com.pos.model.SaleItem;
import com.pos.model.SaleStatus;
import com.pos.repository.ExpenseRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.SaleRepository;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Generates CSV exports for sales & inventory - opened directly in Excel/Sheets. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ExpenseRepository expenseRepository;

    /** Ringkasan transaksi (satu baris per invoice) dalam rentang tanggal. */
    public byte[] generateSalesSummaryCsv(LocalDate from, LocalDate to) {
        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateAsc(startOf(from), endOf(to));

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {
            "No Invoice", "Tanggal", "Kasir", "Pelanggan", "Subtotal", "Diskon", "Pajak", "Total",
            "Metode Bayar", "Status"
        });
        for (Sale sale : sales) {
            rows.add(new String[] {
                sale.getInvoiceNumber(),
                DATE_FORMAT.format(sale.getSaleDate()),
                sale.getCashier().getFullName(),
                sale.getCustomer() != null ? sale.getCustomer().getName() : "-",
                sale.getSubtotal().toPlainString(),
                sale.getDiscountAmount().toPlainString(),
                sale.getTaxAmount().toPlainString(),
                sale.getTotal().toPlainString(),
                sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : "HUTANG",
                sale.getStatus().name()
            });
        }
        return toCsvBytes(rows);
    }

    /** Rincian per item produk terjual (satu baris per baris item) dalam rentang tanggal. */
    public byte[] generateSalesDetailCsv(LocalDate from, LocalDate to) {
        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateAsc(startOf(from), endOf(to));

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {"No Invoice", "Tanggal", "Produk", "Qty", "Harga", "Subtotal"});
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                rows.add(new String[] {
                    sale.getInvoiceNumber(),
                    DATE_FORMAT.format(sale.getSaleDate()),
                    item.getProductName(),
                    String.valueOf(item.getQuantity()),
                    item.getPrice().toPlainString(),
                    item.getSubtotal().toPlainString()
                });
            }
        }
        return toCsvBytes(rows);
    }

    /** "10 ml=10000; 35 ml=35000" - harga jual per ukuran untuk produk per takaran. */
    private static String sizePriceList(Product product) {
        String unit = product.getCategory().getBaseUnit();
        return product.getSizePrices().stream()
                .sorted(java.util.Comparator.comparing(com.pos.model.ProductSizePrice::getSizeAmount))
                .map(p -> p.getSizeAmount().stripTrailingZeros().toPlainString() + " " + unit + "=" + p.getSellPrice().toPlainString())
                .collect(java.util.stream.Collectors.joining("; "));
    }

    /** Laporan stok produk saat ini (untuk audit inventori). */
    public byte[] generateProductStockCsv() {
        List<Product> products = productRepository.findAll();

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {
            "SKU", "Nama Produk", "Kategori", "Satuan", "Harga Beli", "Harga Jual", "Stok", "Stok Minimum", "Status"
        });
        for (Product product : products) {
            boolean measured = product.isMeasured();
            String baseUnit = measured ? " " + product.getCategory().getBaseUnit() : "";
            rows.add(new String[] {
                product.getSku(),
                product.getName(),
                product.getCategory() != null ? product.getCategory().getName() : "-",
                measured ? product.getCategory().getBaseUnit()
                        : product.getUnit() != null ? product.getUnit().getSymbol() : "-",
                measured ? product.getMaterialPricePerUnit().toPlainString() + " /" + baseUnit.trim()
                        : product.getPurchasePrice().toPlainString(),
                measured ? sizePriceList(product) : product.getSellPrice().toPlainString(),
                measured ? product.getMaterialStock().stripTrailingZeros().toPlainString() + baseUnit
                        : String.valueOf(product.getStock()),
                measured ? product.getMinMaterialStock().stripTrailingZeros().toPlainString() + baseUnit
                        : String.valueOf(product.getMinStock()),
                product.isActive() ? "Aktif" : "Nonaktif"
            });
        }
        return toCsvBytes(rows);
    }

    /**
     * Laporan keuangan gabungan: ringkasan + detail penjualan + detail pengeluaran.
     * Satu file CSV dengan tiga bagian dipisah baris kosong.
     */
    public byte[] generateFinancialReportCsv(LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate effectiveTo   = to   == null ? LocalDate.now() : to;

        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateAsc(
                startOf(effectiveFrom), endOf(effectiveTo));
        List<Expense> expenses = expenseRepository
                .findByFilter(effectiveFrom, effectiveTo, null, Pageable.unpaged())
                .getContent();

        BigDecimal totalPenjualan = sales.stream()
                .filter(s -> s.getStatus() == SaleStatus.COMPLETED)
                .map(Sale::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPengeluaran = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal laba = totalPenjualan.subtract(totalPengeluaran);

        List<String[]> rows = new ArrayList<>();

        // ── Ringkasan ──────────────────────────────────────────────────
        rows.add(new String[]{"=== RINGKASAN ==="});
        rows.add(new String[]{"Periode", effectiveFrom + " s/d " + effectiveTo});
        rows.add(new String[]{"Total Penjualan (Rp)", totalPenjualan.toPlainString()});
        rows.add(new String[]{"Total Pengeluaran (Rp)", totalPengeluaran.toPlainString()});
        rows.add(new String[]{"Laba Kotor (Rp)", laba.toPlainString()});
        rows.add(new String[]{""});

        // ── Detail penjualan ───────────────────────────────────────────
        rows.add(new String[]{"=== DETAIL PENJUALAN ==="});
        rows.add(new String[]{"No Invoice", "Tanggal", "Kasir", "Pelanggan",
                "Subtotal", "Diskon", "Pajak", "Total", "Metode Bayar", "Status"});
        for (Sale sale : sales) {
            rows.add(new String[]{
                sale.getInvoiceNumber(),
                DATE_FORMAT.format(sale.getSaleDate()),
                sale.getCashier().getFullName(),
                sale.getCustomer() != null ? sale.getCustomer().getName() : "-",
                sale.getSubtotal().toPlainString(),
                sale.getDiscountAmount().toPlainString(),
                sale.getTaxAmount().toPlainString(),
                sale.getTotal().toPlainString(),
                sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : "HUTANG",
                sale.getStatus().name()
            });
        }
        rows.add(new String[]{""});

        // ── Detail pengeluaran ─────────────────────────────────────────
        rows.add(new String[]{"=== DETAIL PENGELUARAN ==="});
        rows.add(new String[]{"Tanggal", "Kategori", "Keterangan", "Supplier", "Jumlah (Rp)"});
        for (Expense expense : expenses) {
            rows.add(new String[]{
                expense.getExpenseDate().toString(),
                expense.getCategory().getName(),
                expense.getDescription() != null ? expense.getDescription() : "-",
                expense.getSupplier() != null ? expense.getSupplier().getName() : "-",
                expense.getAmount().toPlainString()
            });
        }

        return toCsvBytes(rows);
    }

    /** Laporan pengeluaran saja dalam rentang tanggal. */
    public byte[] generateExpensesCsv(LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate effectiveTo   = to   == null ? LocalDate.now() : to;

        List<Expense> expenses = expenseRepository
                .findByFilter(effectiveFrom, effectiveTo, null, Pageable.unpaged())
                .getContent();

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Tanggal", "Kategori", "Keterangan", "Supplier", "Jumlah (Rp)"});
        for (Expense expense : expenses) {
            rows.add(new String[]{
                expense.getExpenseDate().toString(),
                expense.getCategory().getName(),
                expense.getDescription() != null ? expense.getDescription() : "-",
                expense.getSupplier() != null ? expense.getSupplier().getName() : "-",
                expense.getAmount().toPlainString()
            });
        }
        rows.add(new String[]{""});
        rows.add(new String[]{"TOTAL", "", "", "", total.toPlainString()});

        return toCsvBytes(rows);
    }

    private java.time.Instant startOf(LocalDate date) {
        LocalDate effective = date == null ? LocalDate.now().minusMonths(1) : date;
        return effective.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private java.time.Instant endOf(LocalDate date) {
        LocalDate effective = date == null ? LocalDate.now() : date;
        return effective.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private byte[] toCsvBytes(List<String[]> rows) {
        try (StringWriter stringWriter = new StringWriter();
                CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            csvWriter.writeAll(rows);
            csvWriter.flush();
            // UTF-8 BOM so Excel opens accented/Rupiah text correctly on Windows.
            String bom = new String(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, StandardCharsets.UTF_8);
            String csv = bom + stringWriter;
            return csv.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal membuat file CSV", e);
        }
    }
}
