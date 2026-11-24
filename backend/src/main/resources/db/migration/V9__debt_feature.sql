-- Tambah kolom payment_status ke sales
ALTER TABLE sales ALTER COLUMN payment_method DROP NOT NULL;

ALTER TABLE sales ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID'
    CHECK (payment_status IN ('PAID', 'UNPAID'));

-- Tabel catatan pelunasan hutang
CREATE TABLE IF NOT EXISTS debt_payments (
    id          UUID          PRIMARY KEY,
    sale_id     UUID          NOT NULL REFERENCES sales(id),
    amount      NUMERIC(19,2) NOT NULL,
    notes       VARCHAR(255),
    cashier_id  UUID          NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    version     BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_debt_payments_sale    ON debt_payments (sale_id);
CREATE INDEX idx_debt_payments_cashier ON debt_payments (cashier_id);
