package com.pos.product.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.pos.config.JpaAuditingConfig;
import com.pos.product.entity.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findLowStockProducts_returnsOnlyActiveProductsAtOrBelowMinimumStock() {
        productRepository.save(lowStockProduct("SKU-LOW", 2, 10));
        productRepository.save(healthyStockProduct("SKU-OK", 100, 10));
        productRepository.save(inactiveLowStockProduct("SKU-INACTIVE", 1, 10));

        var result = productRepository.findLowStockProducts();

        assertThat(result).extracting(Product::getSku).containsExactly("SKU-LOW");
    }

    private Product lowStockProduct(String sku, int stock, int minStock) {
        return Product.builder()
                .sku(sku).name("Produk " + sku)
                .purchasePrice(new BigDecimal("1000")).sellPrice(new BigDecimal("1500"))
                .stock(stock).minStock(minStock).active(true)
                .build();
    }

    private Product healthyStockProduct(String sku, int stock, int minStock) {
        return lowStockProduct(sku, stock, minStock);
    }

    private Product inactiveLowStockProduct(String sku, int stock, int minStock) {
        Product product = lowStockProduct(sku, stock, minStock);
        product.setActive(false);
        return product;
    }
}
