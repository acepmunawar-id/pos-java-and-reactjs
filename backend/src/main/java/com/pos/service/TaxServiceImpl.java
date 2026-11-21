package com.pos.service;

import com.pos.exception.ResourceNotFoundException;
import com.pos.dto.TaxRequest;
import com.pos.dto.TaxResponse;
import com.pos.model.Tax;
import com.pos.mapper.TaxMapper;
import com.pos.repository.TaxRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxServiceImpl implements TaxService {

    private final TaxRepository taxRepository;
    private final TaxMapper taxMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TaxResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return taxRepository.findByNameContainingIgnoreCase(keyword, pageable).map(taxMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxResponse> findAllActive() {
        return taxRepository.findByActiveTrue().stream().map(taxMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaxResponse findById(UUID id) {
        return taxMapper.toResponse(getOrThrow(id));
    }

    @Override
    public TaxResponse create(TaxRequest request) {
        return taxMapper.toResponse(taxRepository.save(taxMapper.toEntity(request)));
    }

    @Override
    public TaxResponse update(UUID id, TaxRequest request) {
        Tax tax = getOrThrow(id);
        taxMapper.updateEntity(request, tax);
        return taxMapper.toResponse(taxRepository.save(tax));
    }

    @Override
    public void delete(UUID id) {
        taxRepository.delete(getOrThrow(id));
    }

    private Tax getOrThrow(UUID id) {
        return taxRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Pajak", id));
    }
}
