-- ==========================================================================
-- V4 : Tabel pengeluaran (uang keluar) - belanja bahan baku & biaya lain.
-- ==========================================================================

CREATE TABLE expenses (
    id             UUID PRIMARY KEY,
    expense_date   DATE          NOT NULL,
    category       VARCHAR(100)  NOT NULL,
    description    VARCHAR(255),
    amount         NUMERIC(19,2) NOT NULL CHECK (amount >= 0),
    supplier_id    UUID REFERENCES suppliers(id),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT        NOT NULL DEFAULT 0
);
CREATE INDEX idx_expenses_date ON expenses (expense_date);
