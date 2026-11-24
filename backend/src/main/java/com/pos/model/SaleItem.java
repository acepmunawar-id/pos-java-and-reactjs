package com.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Line item of a {@link Sale}. Product name & price are snapshotted at sale
 * time so historical receipts stay accurate even if the product is later
 * renamed or repriced.
 */
@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "sale")
public class SaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    // ---- Snapshot produk per takaran (null untuk produk umum) ----

    /** Ukuran kemasan yang dijual (ml / gram). */
    @Column(name = "size_amount", precision = 10, scale = 2)
    private BigDecimal sizeAmount;

    /** Total bahan utama terpakai untuk baris ini (sudah dikali qty). */
    @Column(name = "material_used", precision = 19, scale = 3)
    private BigDecimal materialUsed;

    /** Total bahan campuran terpakai untuk baris ini (sudah dikali qty). */
    @Column(name = "mixer_used", precision = 19, scale = 3)
    private BigDecimal mixerUsed;

    /** Modal per kemasan saat transaksi. */
    @Column(name = "unit_cost", precision = 19, scale = 2)
    private BigDecimal unitCost;
}
