package com.pos.partner.service;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.entity.Supplier;
import com.pos.partner.mapper.SupplierMapper;
import com.pos.partner.repository.SupplierRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return supplierRepository.findByNameContainingIgnoreCase(keyword, pageable).map(supplierMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponse findById(UUID id) {
        return supplierMapper.toResponse(getOrThrow(id));
    }

    @Override
    public PartnerResponse create(PartnerRequest request) {
        return supplierMapper.toResponse(supplierRepository.save(supplierMapper.toEntity(request)));
    }

    @Override
    public PartnerResponse update(UUID id, PartnerRequest request) {
        Supplier supplier = getOrThrow(id);
        supplierMapper.updateEntity(request, supplier);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    public void delete(UUID id) {
        supplierRepository.delete(getOrThrow(id));
    }

    private Supplier getOrThrow(UUID id) {
        return supplierRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }
}
