package com.pos.repository;

import com.pos.model.Tax;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, UUID> {
    Page<Tax> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Tax> findByActiveTrue();
}
