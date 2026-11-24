package com.pos.repository;

import com.pos.model.DebtPayment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, UUID> {

    List<DebtPayment> findBySaleIdOrderByCreatedAtDesc(UUID saleId);

    @Query("SELECT COALESCE(SUM(dp.amount), 0) FROM DebtPayment dp WHERE dp.sale.id = :saleId")
    BigDecimal sumBySaleId(@Param("saleId") UUID saleId);
}
