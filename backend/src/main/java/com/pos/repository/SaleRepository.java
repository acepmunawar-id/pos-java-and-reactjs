package com.pos.repository;

import com.pos.model.Sale;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    Page<Sale> findBySaleDateBetween(Instant from, Instant to, Pageable pageable);

    Page<Sale> findByCashierIdAndSaleDateBetween(UUID cashierId, Instant from, Instant to, Pageable pageable);

    List<Sale> findBySaleDateBetweenOrderBySaleDateAsc(Instant from, Instant to);

    List<Sale> findByCashierIdAndSaleDateBetweenOrderBySaleDateAsc(UUID cashierId, Instant from, Instant to);

    @Query(value = "select nextval('sale_invoice_seq')", nativeQuery = true)
    Long nextInvoiceSequence();
}
