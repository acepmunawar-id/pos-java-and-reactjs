package com.pos.pricing.entity;

import com.pos.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "taxes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Tax extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    /** Persentase, mis. 11.00 untuk PPN 11%. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
