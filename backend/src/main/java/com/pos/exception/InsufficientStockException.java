package com.pos.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, int available, int requested) {
        super("Stok produk '" + productName + "' tidak mencukupi. Tersedia: " + available + ", diminta: " + requested);
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
