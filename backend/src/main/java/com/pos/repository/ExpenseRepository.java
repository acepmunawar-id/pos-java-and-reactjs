package com.pos.repository;

import com.pos.model.Expense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("""
        SELECT e FROM Expense e
        JOIN FETCH e.category
        LEFT JOIN FETCH e.supplier
        WHERE (:from       IS NULL OR e.expenseDate  >= :from)
          AND (:to         IS NULL OR e.expenseDate  <= :to)
          AND (:categoryId IS NULL OR e.category.id  = :categoryId)
        ORDER BY e.expenseDate DESC
        """)
    Page<Expense> findByFilter(
            @Param("from")       LocalDate from,
            @Param("to")         LocalDate to,
            @Param("categoryId") UUID categoryId,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal sumAmountAll();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate <= :to")
    BigDecimal sumAmountTo(@Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate >= :from AND e.expenseDate <= :to")
    BigDecimal sumAmountBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
