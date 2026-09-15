package com.pos.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.pricing.dto.DiscountRequest;
import com.pos.pricing.dto.DiscountResponse;
import com.pos.pricing.entity.Discount;
import com.pos.pricing.entity.DiscountType;
import com.pos.pricing.mapper.DiscountMapper;
import com.pos.pricing.repository.DiscountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountServiceImpl discountService;

    private DiscountRequest request;

    @BeforeEach
    void setUp() {
        DiscountMapper mapper = Mappers.getMapper(DiscountMapper.class);
        discountService = new DiscountServiceImpl(discountRepository, mapper);

        request = new DiscountRequest();
        request.setName("Diskon Member 10%");
        request.setType(DiscountType.PERCENTAGE);
        request.setValue(new BigDecimal("10"));
        request.setActive(true);
    }

    @Test
    void create_savesDiscount() {
        when(discountRepository.save(any(Discount.class))).thenAnswer(inv -> inv.getArgument(0));

        DiscountResponse response = discountService.create(request);

        assertThat(response.getName()).isEqualTo("Diskon Member 10%");
        assertThat(response.getType()).isEqualTo(DiscountType.PERCENTAGE);
    }

    @Test
    void findAllActive_returnsOnlyActiveDiscounts() {
        Discount active = Discount.builder()
                .id(UUID.randomUUID()).name("Diskon Aktif").type(DiscountType.NOMINAL)
                .value(new BigDecimal("5000")).active(true).build();
        when(discountRepository.findByActiveTrue()).thenReturn(List.of(active));

        List<DiscountResponse> result = discountService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Diskon Aktif");
    }

    @Test
    void findById_throwsResourceNotFound_whenDiscountMissing() {
        UUID id = UUID.randomUUID();
        when(discountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_changesValue_whenDiscountFound() {
        UUID id = UUID.randomUUID();
        Discount existing = Discount.builder()
                .id(id).name("Lama").type(DiscountType.NOMINAL).value(new BigDecimal("1000")).active(true).build();
        when(discountRepository.findById(id)).thenReturn(Optional.of(existing));
        when(discountRepository.save(any(Discount.class))).thenAnswer(inv -> inv.getArgument(0));

        DiscountResponse response = discountService.update(id, request);

        assertThat(response.getType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(response.getValue()).isEqualByComparingTo("10");
    }
}
