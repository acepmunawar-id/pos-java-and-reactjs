package com.pos.repository;

import com.pos.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrBarcodeContainingIgnoreCase(
            String name, String sku, String barcode, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.name")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.id = :categoryId ORDER BY p.name")
    Page<Product> findAllActiveByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%',:search,'%'))) ORDER BY p.name")
    Page<Product> findAllActiveBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.id = :categoryId AND (LOWER(p.name) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%',:search,'%'))) ORDER BY p.name")
    Page<Product> findAllActiveBySearchAndCategory(@Param("search") String search, @Param("categoryId") UUID categoryId, Pageable pageable);

    Optional<Product> findByBarcode(String barcode);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, UUID id);

    @Query("""
            select p from Product p left join p.category c
            where p.active = true and (
                (c.type = com.pos.model.CategoryType.MEASURED and p.materialStock <= p.minMaterialStock)
                or ((c is null or c.type <> com.pos.model.CategoryType.MEASURED) and p.stock <= p.minStock))
            order by p.name""")
    List<Product> findLowStockProducts();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(UUID id);
}
