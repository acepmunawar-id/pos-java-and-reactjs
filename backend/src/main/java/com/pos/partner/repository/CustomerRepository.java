package com.pos.partner.repository;

import com.pos.partner.entity.Customer;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
