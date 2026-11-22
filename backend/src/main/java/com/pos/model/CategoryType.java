package com.pos.model;

/** Cara produk di dalam kategori dihitung. */
public enum CategoryType {
    /** Produk biasa: harga jual tetap, stok dalam satuan barang (pcs). */
    GENERAL,
    /**
     * Produk per takaran: stok bahan dalam satuan dasar (ml/gram), dijual per
     * ukuran kemasan, dengan bahan campuran opsional. Mis. minyak kelapa,
     * gula merah, parfum refill.
     */
    MEASURED
}
