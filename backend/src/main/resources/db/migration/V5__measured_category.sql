-- ==========================================================================
-- V5 : Kategori tipe PER TAKARAN.
--   GENERAL  : produk biasa - harga jual tetap, stok dalam satuan barang (pcs).
--   MEASURED : stok bahan disimpan dalam satuan dasar (ml / gram) lalu dijual
--              per ukuran kemasan. Contoh: minyak kelapa 250/500/1000 ml,
--              gula merah 250/500 gram, parfum refill 10/35/50 ml.
--              Bahan campuran bersifat OPSIONAL (mis. alkohol pada parfum,
--              rasio bahan utama : campuran = 2 : 1).
--   Modal per kemasan = takaran bahan utama x harga/satuan
--                     + takaran campuran x harga campuran/satuan
--                     + harga kemasan (botol/plastik).
-- ==========================================================================

ALTER TABLE categories
    ADD COLUMN type                  VARCHAR(20)   NOT NULL DEFAULT 'GENERAL' CHECK (type IN ('GENERAL', 'MEASURED')),
    ADD COLUMN base_unit             VARCHAR(10),
    ADD COLUMN material_ratio        NUMERIC(10,2),
    ADD COLUMN mixer_ratio           NUMERIC(10,2),
    ADD COLUMN mixer_name            VARCHAR(50),
    ADD COLUMN mixer_price_per_unit  NUMERIC(19,4),
    ADD COLUMN mixer_stock           NUMERIC(19,3) NOT NULL DEFAULT 0;

-- Ukuran kemasan yang dijual di kategori per takaran + harga kemasan kosongnya.
CREATE TABLE category_sizes (
    category_id      UUID          NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    size_amount      NUMERIC(10,2) NOT NULL CHECK (size_amount > 0),
    packaging_price  NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (packaging_price >= 0),
    PRIMARY KEY (category_id, size_amount)
);

ALTER TABLE products
    ADD COLUMN material_price_per_unit NUMERIC(19,4),
    ADD COLUMN material_stock          NUMERIC(19,3) NOT NULL DEFAULT 0,
    ADD COLUMN min_material_stock      NUMERIC(19,3) NOT NULL DEFAULT 0;

-- Harga jual produk per takaran untuk tiap ukuran kemasan.
CREATE TABLE product_size_prices (
    product_id    UUID          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    size_amount   NUMERIC(10,2) NOT NULL,
    sell_price    NUMERIC(19,2) NOT NULL CHECK (sell_price >= 0),
    PRIMARY KEY (product_id, size_amount)
);

-- Snapshot ukuran, pemakaian bahan & modal saat transaksi (untuk void & laba).
ALTER TABLE sale_items
    ADD COLUMN size_amount   NUMERIC(10,2),
    ADD COLUMN material_used NUMERIC(19,3),
    ADD COLUMN mixer_used    NUMERIC(19,3),
    ADD COLUMN unit_cost     NUMERIC(19,2);
