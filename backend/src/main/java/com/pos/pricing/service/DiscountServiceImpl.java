package com.pos.pricing.service;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.pricing.dto.DiscountRequest;
import com.pos.pricing.dto.DiscountResponse;
import com.pos.pricing.entity.Discount;
import com.pos.pricing.mapper.DiscountMapper;
import com.pos.pricing.repository.DiscountRepository;
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
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountMapper discountMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DiscountResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return discountRepository.findByNameContainingIgnoreCase(keyword, pageable).map(discountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> findAllActive() {
        return discountRepository.findByActiveTrue().stream().map(discountMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse findById(UUID id) {
        return discountMapper.toResponse(getOrThrow(id));
    }

    @Override
    public DiscountResponse create(DiscountRequest request) {
        return discountMapper.toResponse(discountRepository.save(discountMapper.toEntity(request)));
    }

    @Override
    public DiscountResponse update(UUID id, DiscountRequest request) {
        Discount discount = getOrThrow(id);
        discountMapper.updateEntity(request, discount);
        return discountMapper.toResponse(discountRepository.save(discount));
    }

    @Override
    public void delete(UUID id) {
        discountRepository.delete(getOrThrow(id));
    }

    private Discount getOrThrow(UUID id) {
        return discountRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Diskon", id));
    }
}
