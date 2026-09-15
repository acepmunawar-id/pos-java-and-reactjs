package com.pos.partner.repository;

import com.pos.partner.entity.Supplier;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
