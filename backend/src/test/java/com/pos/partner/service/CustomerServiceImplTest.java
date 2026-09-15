package com.pos.partner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.entity.Customer;
import com.pos.partner.mapper.CustomerMapper;
import com.pos.partner.repository.CustomerRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    private PartnerRequest request;

    @BeforeEach
    void setUp() {
        CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);
        customerService = new CustomerServiceImpl(customerRepository, mapper);

        request = new PartnerRequest();
        request.setName("Budi Santoso");
        request.setPhone("0812-0000-0000");
        request.setEmail("budi@example.com");
        request.setAddress("Jl. Melati No. 1");
    }

    @Test
    void create_savesCustomer() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        PartnerResponse response = customerService.create(request);

        assertThat(response.getName()).isEqualTo("Budi Santoso");
        assertThat(response.getPhone()).isEqualTo("0812-0000-0000");
    }

    @Test
    void findById_throwsResourceNotFound_whenCustomerMissing() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_updatesFields_whenCustomerFound() {
        UUID id = UUID.randomUUID();
        Customer existing = Customer.builder().id(id).name("Old Name").build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        PartnerResponse response = customerService.update(id, request);

        assertThat(response.getName()).isEqualTo("Budi Santoso");
    }

    @Test
    void delete_removesCustomer_whenFound() {
        UUID id = UUID.randomUUID();
        Customer existing = Customer.builder().id(id).name("Budi Santoso").build();
        when(customerRepository.findById(id)).thenReturn(Optional.of(existing));

        customerService.delete(id);

        verify(customerRepository).delete(existing);
    }
}
