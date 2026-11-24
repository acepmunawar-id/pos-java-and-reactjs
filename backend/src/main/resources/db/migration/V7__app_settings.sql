-- ==========================================================================
-- V7 : Tabel app_settings — konfigurasi aplikasi (key-value store).
-- ==========================================================================

CREATE TABLE app_settings (
    key         VARCHAR(100) PRIMARY KEY,
    value       TEXT,
    description VARCHAR(255),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by  VARCHAR(100)
);

-- Seed nilai default
INSERT INTO app_settings (key, value, description) VALUES
    ('modal_awal',  '0',            'Modal awal uang tunai (Rp). Digunakan untuk menghitung Saldo Sekarang.'),
    ('nama_toko',   'Shafel Parfum','Nama toko yang tampil di struk.'),
    ('alamat_toko', '',             'Alamat toko yang tampil di struk.'),
    ('no_telp',     '',             'Nomor telepon toko yang tampil di struk.');
