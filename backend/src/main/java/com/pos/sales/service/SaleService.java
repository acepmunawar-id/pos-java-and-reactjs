package com.pos.sales.service;

import com.pos.sales.dto.SaleRequest;
import com.pos.sales.dto.SaleResponse;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {

    /** Memproses transaksi baru: validasi stok, hitung total, kurangi stok, simpan invoice. */
    SaleResponse create(SaleRequest request);

    Page<SaleResponse> findAll(LocalDate from, LocalDate to, Pageable pageable);

    SaleResponse findById(UUID id);

    /** Membatalkan transaksi yang sudah selesai dan mengembalikan stok produk terkait. */
    SaleResponse voidSale(UUID id);
}
