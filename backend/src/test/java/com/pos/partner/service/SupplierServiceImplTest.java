package com.pos.partner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.entity.Supplier;
import com.pos.partner.mapper.SupplierMapper;
import com.pos.partner.repository.SupplierRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    private SupplierServiceImpl supplierService;

    private PartnerRequest request;

    @BeforeEach
    void setUp() {
        SupplierMapper mapper = Mappers.getMapper(SupplierMapper.class);
        supplierService = new SupplierServiceImpl(supplierRepository, mapper);

        request = new PartnerRequest();
        request.setName("PT Sumber Makmur");
        request.setPhone("021-555000");
        request.setEmail("cs@sumbermakmur.co.id");
        request.setAddress("Jl. Industri No. 10");
    }

    @Test
    void create_savesSupplier() {
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        PartnerResponse response = supplierService.create(request);

        assertThat(response.getName()).isEqualTo("PT Sumber Makmur");
    }

    @Test
    void findById_throwsResourceNotFound_whenSupplierMissing() {
        UUID id = UUID.randomUUID();
        when(supplierRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_updatesFields_whenSupplierFound() {
        UUID id = UUID.randomUUID();
        Supplier existing = Supplier.builder().id(id).name("Old Supplier").build();
        when(supplierRepository.findById(id)).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        PartnerResponse response = supplierService.update(id, request);

        assertThat(response.getName()).isEqualTo("PT Sumber Makmur");
    }

    @Test
    void delete_removesSupplier_whenFound() {
        UUID id = UUID.randomUUID();
        Supplier existing = Supplier.builder().id(id).name("PT Sumber Makmur").build();
        when(supplierRepository.findById(id)).thenReturn(Optional.of(existing));

        supplierService.delete(id);

        verify(supplierRepository).delete(existing);
    }
}
