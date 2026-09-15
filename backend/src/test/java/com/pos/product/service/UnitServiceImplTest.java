package com.pos.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.UnitRequest;
import com.pos.product.dto.UnitResponse;
import com.pos.product.entity.Unit;
import com.pos.product.mapper.UnitMapper;
import com.pos.product.repository.UnitRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnitServiceImplTest {

    @Mock
    private UnitRepository unitRepository;

    private UnitServiceImpl unitService;

    private UnitRequest request;

    @BeforeEach
    void setUp() {
        UnitMapper mapper = Mappers.getMapper(UnitMapper.class);
        unitService = new UnitServiceImpl(unitRepository, mapper);

        request = new UnitRequest();
        request.setName("Pieces");
        request.setSymbol("pcs");
    }

    @Test
    void create_savesUnit_whenNameNotTaken() {
        when(unitRepository.existsByNameIgnoreCase("Pieces")).thenReturn(false);
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> inv.getArgument(0));

        UnitResponse response = unitService.create(request);

        assertThat(response.getSymbol()).isEqualTo("pcs");
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(unitRepository.existsByNameIgnoreCase("Pieces")).thenReturn(true);

        assertThatThrownBy(() -> unitService.create(request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void findById_throwsResourceNotFound_whenUnitMissing() {
        UUID id = UUID.randomUUID();
        when(unitRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unitService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_throwsDuplicateResourceException_whenRenamedToExistingUnit() {
        UUID id = UUID.randomUUID();
        Unit existing = Unit.builder().id(id).name("Box").symbol("box").build();
        when(unitRepository.findById(id)).thenReturn(Optional.of(existing));
        when(unitRepository.existsByNameIgnoreCaseAndIdNot("Pieces", id)).thenReturn(true);

        assertThatThrownBy(() -> unitService.update(id, request)).isInstanceOf(DuplicateResourceException.class);
    }
}
