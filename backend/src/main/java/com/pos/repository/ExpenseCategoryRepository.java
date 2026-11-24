package com.pos.repository;

import com.pos.model.ExpenseCategory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {
}
