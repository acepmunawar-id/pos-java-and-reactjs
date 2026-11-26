-- ==========================================================================
-- V2__seed.sql : Data awal (default accounts & master data dasar)
-- Password di-hash memakai pgcrypto (bcrypt/"bf") agar kompatibel dengan
-- Spring Security BCryptPasswordEncoder (format hash $2a$).
-- Password default untuk SEMUA akun contoh: Password123!
-- WAJIB diganti setelah instalasi pertama (lihat docs/USER_GUIDE.md).
-- ==========================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (id, username, password, full_name, email, role, active)
VALUES
    (gen_random_uuid(), 'superadmin', crypt('Password123!', gen_salt('bf', 10)),
     'Super Administrator', 'superadmin@pos.local', 'SUPER_ADMIN', TRUE),
    (gen_random_uuid(), 'admin', crypt('Password123!', gen_salt('bf', 10)),
     'Administrator Toko', 'admin@pos.local', 'ADMIN', TRUE),
    (gen_random_uuid(), 'kasir', crypt('Password123!', gen_salt('bf', 10)),
     'Kasir Toko', 'kasir@pos.local', 'USER', TRUE)
ON CONFLICT (username) DO NOTHING;

INSERT INTO units (id, name, symbol) VALUES
    (gen_random_uuid(), 'Pieces', 'pcs'),
    (gen_random_uuid(), 'Kilogram', 'kg'),
    (gen_random_uuid(), 'Box', 'box')
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (id, name, description) VALUES
    (gen_random_uuid(), 'Umum', 'Kategori umum/serba-guna untuk produk yang belum dikelompokkan')
ON CONFLICT (name) DO NOTHING;

INSERT INTO taxes (id, name, rate, active) VALUES
    (gen_random_uuid(), 'PPN 11%', 11.00, TRUE),
    (gen_random_uuid(), 'Tanpa Pajak', 0.00, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO discounts (id, name, type, discount_value, active) VALUES
    (gen_random_uuid(), 'Tanpa Diskon', 'NOMINAL', 0.00, TRUE)
ON CONFLICT DO NOTHING;
