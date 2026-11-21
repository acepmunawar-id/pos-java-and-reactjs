package com.pos.service;

import com.pos.dto.SaleRequest;
import com.pos.dto.SaleResponse;
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
