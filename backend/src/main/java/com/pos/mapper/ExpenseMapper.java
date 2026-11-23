package com.pos.mapper;

import com.pos.dto.ExpenseCategoryResponse;
import com.pos.dto.ExpenseRequest;
import com.pos.dto.ExpenseResponse;
import com.pos.model.Expense;
import com.pos.model.ExpenseCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "supplierId",   source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    ExpenseResponse toResponse(Expense expense);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    Expense toEntity(ExpenseRequest request);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    void updateEntity(ExpenseRequest request, @MappingTarget Expense expense);

    ExpenseCategoryResponse toCategoryResponse(ExpenseCategory category);
}
