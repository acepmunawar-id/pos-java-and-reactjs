-- ==========================================================================
-- V6 : Tabel expense_categories — master kategori pengeluaran.
--      Menggantikan kolom category VARCHAR di expenses.
-- ==========================================================================

CREATE TABLE expense_categories (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    version     BIGINT       NOT NULL DEFAULT 0
);

-- Seed kategori dasar
INSERT INTO expense_categories (id, name, description) VALUES
    (gen_random_uuid(), 'Belanja Bahan',  'Pembelian biang parfum, alkohol, dan bahan baku lainnya'),
    (gen_random_uuid(), 'Botol & Kemasan','Pembelian botol, tutup, kemasan, dan label'),
    (gen_random_uuid(), 'Lain-lain',      'Pengeluaran operasional lain yang tidak masuk kategori di atas');

-- Tambah kolom category_id ke expenses, lalu hapus kolom category lama
ALTER TABLE expenses ADD COLUMN category_id UUID REFERENCES expense_categories(id);

-- Migrasi data lama: mapping nilai string lama ke id kategori baru
UPDATE expenses e
SET category_id = ec.id
FROM expense_categories ec
WHERE ec.name = CASE e.category
    WHEN 'BELANJA_BAHAN'  THEN 'Belanja Bahan'
    WHEN 'BOTOL_KEMASAN'  THEN 'Botol & Kemasan'
    ELSE 'Lain-lain'
END;

-- Setelah migrasi data, jadikan NOT NULL dan drop kolom lama
ALTER TABLE expenses ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE expenses DROP COLUMN category;

CREATE INDEX idx_expense_categories_name ON expense_categories (name);
