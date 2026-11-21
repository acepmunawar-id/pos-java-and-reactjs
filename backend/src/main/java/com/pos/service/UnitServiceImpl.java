package com.pos.service;

import com.pos.exception.DuplicateResourceException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.dto.UnitRequest;
import com.pos.dto.UnitResponse;
import com.pos.model.Unit;
import com.pos.mapper.UnitMapper;
import com.pos.repository.UnitRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UnitResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return unitRepository.findByNameContainingIgnoreCase(keyword, pageable).map(unitMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse findById(UUID id) {
        return unitMapper.toResponse(getOrThrow(id));
    }

    @Override
    public UnitResponse create(UnitRequest request) {
        if (unitRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Satuan '" + request.getName() + "' sudah ada");
        }
        return unitMapper.toResponse(unitRepository.save(unitMapper.toEntity(request)));
    }

    @Override
    public UnitResponse update(UUID id, UnitRequest request) {
        Unit unit = getOrThrow(id);
        if (unitRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Satuan '" + request.getName() + "' sudah ada");
        }
        unitMapper.updateEntity(request, unit);
        return unitMapper.toResponse(unitRepository.save(unit));
    }

    @Override
    public void delete(UUID id) {
        unitRepository.delete(getOrThrow(id));
    }

    private Unit getOrThrow(UUID id) {
        return unitRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Satuan", id));
    }
}
