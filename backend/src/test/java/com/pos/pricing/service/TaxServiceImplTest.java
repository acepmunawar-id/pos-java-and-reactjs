package com.pos.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.pricing.dto.TaxRequest;
import com.pos.pricing.dto.TaxResponse;
import com.pos.pricing.entity.Tax;
import com.pos.pricing.mapper.TaxMapper;
import com.pos.pricing.repository.TaxRepository;
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
class TaxServiceImplTest {

    @Mock
    private TaxRepository taxRepository;

    private TaxServiceImpl taxService;

    private TaxRequest request;

    @BeforeEach
    void setUp() {
        TaxMapper mapper = Mappers.getMapper(TaxMapper.class);
        taxService = new TaxServiceImpl(taxRepository, mapper);

        request = new TaxRequest();
        request.setName("PPN 11%");
        request.setRate(new BigDecimal("11"));
        request.setActive(true);
    }

    @Test
    void create_savesTax() {
        when(taxRepository.save(any(Tax.class))).thenAnswer(inv -> inv.getArgument(0));

        TaxResponse response = taxService.create(request);

        assertThat(response.getName()).isEqualTo("PPN 11%");
        assertThat(response.getRate()).isEqualByComparingTo("11");
    }

    @Test
    void findAllActive_returnsOnlyActiveTaxes() {
        Tax activeTax = Tax.builder().id(UUID.randomUUID()).name("PPN 11%").rate(new BigDecimal("11")).active(true).build();
        when(taxRepository.findByActiveTrue()).thenReturn(List.of(activeTax));

        List<TaxResponse> result = taxService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PPN 11%");
    }

    @Test
    void findById_throwsResourceNotFound_whenTaxMissing() {
        UUID id = UUID.randomUUID();
        when(taxRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_changesRate_whenTaxFound() {
        UUID id = UUID.randomUUID();
        Tax existing = Tax.builder().id(id).name("PPN Lama").rate(new BigDecimal("10")).active(true).build();
        when(taxRepository.findById(id)).thenReturn(Optional.of(existing));
        when(taxRepository.save(any(Tax.class))).thenAnswer(inv -> inv.getArgument(0));

        TaxResponse response = taxService.update(id, request);

        assertThat(response.getRate()).isEqualByComparingTo("11");
    }
}
