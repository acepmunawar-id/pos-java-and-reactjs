package com.pos.service;

import com.pos.exception.ResourceNotFoundException;
import com.pos.dto.PartnerRequest;
import com.pos.dto.PartnerResponse;
import com.pos.model.Customer;
import com.pos.mapper.CustomerMapper;
import com.pos.repository.CustomerRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return customerRepository.findByNameContainingIgnoreCase(keyword, pageable).map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponse findById(UUID id) {
        return customerMapper.toResponse(getOrThrow(id));
    }

    @Override
    public PartnerResponse create(PartnerRequest request) {
        return customerMapper.toResponse(customerRepository.save(customerMapper.toEntity(request)));
    }

    @Override
    public PartnerResponse update(UUID id, PartnerRequest request) {
        Customer customer = getOrThrow(id);
        customerMapper.updateEntity(request, customer);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public void delete(UUID id) {
        customerRepository.delete(getOrThrow(id));
    }

    private Customer getOrThrow(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Pelanggan", id));
    }
}
