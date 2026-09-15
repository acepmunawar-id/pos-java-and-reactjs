-- ==========================================================================
-- V1__init.sql : Skema awal database POS App
-- ==========================================================================

CREATE TABLE users (
    id             UUID PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150),
    role           VARCHAR(20)  NOT NULL CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'USER')),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE categories (
    id             UUID PRIMARY KEY,
    name           VARCHAR(100) NOT NULL UNIQUE,
    description    VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE units (
    id             UUID PRIMARY KEY,
    name           VARCHAR(50)  NOT NULL UNIQUE,
    symbol         VARCHAR(10)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE products (
    id              UUID PRIMARY KEY,
    sku             VARCHAR(50)  NOT NULL UNIQUE,
    barcode         VARCHAR(100),
    name            VARCHAR(150) NOT NULL,
    category_id     UUID REFERENCES categories(id),
    unit_id         UUID REFERENCES units(id),
    purchase_price  NUMERIC(19,2) NOT NULL DEFAULT 0,
    sell_price      NUMERIC(19,2) NOT NULL DEFAULT 0,
    stock           INTEGER       NOT NULL DEFAULT 0,
    min_stock       INTEGER       NOT NULL DEFAULT 0,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP     NOT NULL DEFAULT now(),
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    version         BIGINT        NOT NULL DEFAULT 0
);
CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_products_barcode ON products (barcode);

CREATE TABLE customers (
    id             UUID PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    email          VARCHAR(150),
    address        VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE suppliers (
    id             UUID PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    phone          VARCHAR(30),
    email          VARCHAR(150),
    address        VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE taxes (
    id             UUID PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    rate           NUMERIC(5,2) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE discounts (
    id             UUID PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    type           VARCHAR(20)  NOT NULL CHECK (type IN ('PERCENTAGE', 'NOMINAL')),
    discount_value NUMERIC(19,2) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE SEQUENCE sale_invoice_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE sales (
    id               UUID PRIMARY KEY,
    invoice_number   VARCHAR(30)   NOT NULL UNIQUE,
    sale_date        TIMESTAMP     NOT NULL DEFAULT now(),
    customer_id      UUID REFERENCES customers(id),
    cashier_id       UUID          NOT NULL REFERENCES users(id),
    tax_id           UUID REFERENCES taxes(id),
    discount_id      UUID REFERENCES discounts(id),
    subtotal         NUMERIC(19,2) NOT NULL DEFAULT 0,
    tax_amount       NUMERIC(19,2) NOT NULL DEFAULT 0,
    discount_amount  NUMERIC(19,2) NOT NULL DEFAULT 0,
    total            NUMERIC(19,2) NOT NULL DEFAULT 0,
    payment_method   VARCHAR(20)   NOT NULL DEFAULT 'CASH' CHECK (payment_method IN ('CASH', 'DEBIT', 'CREDIT', 'QRIS', 'TRANSFER')),
    amount_paid      NUMERIC(19,2) NOT NULL DEFAULT 0,
    change_amount    NUMERIC(19,2) NOT NULL DEFAULT 0,
    status           VARCHAR(20)   NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED', 'VOID')),
    created_at       TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP     NOT NULL DEFAULT now(),
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),
    version          BIGINT        NOT NULL DEFAULT 0
);
CREATE INDEX idx_sales_sale_date ON sales (sale_date);
CREATE INDEX idx_sales_cashier ON sales (cashier_id);

CREATE TABLE sale_items (
    id             UUID PRIMARY KEY,
    sale_id        UUID          NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id     UUID          NOT NULL REFERENCES products(id),
    product_name   VARCHAR(150)  NOT NULL,
    price          NUMERIC(19,2) NOT NULL,
    quantity       INTEGER       NOT NULL,
    subtotal       NUMERIC(19,2) NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT now(),
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    version        BIGINT        NOT NULL DEFAULT 0
);
CREATE INDEX idx_sale_items_sale ON sale_items (sale_id);
