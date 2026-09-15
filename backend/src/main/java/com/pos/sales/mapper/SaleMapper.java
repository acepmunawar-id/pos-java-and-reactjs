package com.pos.sales.mapper;

import com.pos.sales.dto.SaleItemResponse;
import com.pos.sales.dto.SaleResponse;
import com.pos.sales.entity.Sale;
import com.pos.sales.entity.SaleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "customerId", expression = "java(sale.getCustomer() != null ? sale.getCustomer().getId() : null)")
    @Mapping(target = "customerName", expression = "java(sale.getCustomer() != null ? sale.getCustomer().getName() : null)")
    @Mapping(target = "cashierId", source = "cashier.id")
    @Mapping(target = "cashierName", source = "cashier.fullName")
    @Mapping(target = "taxName", expression = "java(sale.getTax() != null ? sale.getTax().getName() : null)")
    @Mapping(target = "discountName", expression = "java(sale.getDiscount() != null ? sale.getDiscount().getName() : null)")
    SaleResponse toResponse(Sale sale);

    @Mapping(target = "productId", source = "product.id")
    SaleItemResponse toItemResponse(SaleItem item);
}
