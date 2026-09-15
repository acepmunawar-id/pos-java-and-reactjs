package com.pos.pricing.repository;

import com.pos.pricing.entity.Discount;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, UUID> {
    Page<Discount> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Discount> findByActiveTrue();
}
