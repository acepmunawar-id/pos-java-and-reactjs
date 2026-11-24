package com.pos.repository;

import com.pos.model.Category;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    /** Kunci baris kategori saat transaksi agar stok bahan campuran tidak minus karena checkout bersamaan. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Category c where c.id = :id")
    Optional<Category> findByIdForUpdate(UUID id);
}
