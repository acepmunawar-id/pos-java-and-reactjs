package com.pos.service;

import com.pos.dto.AppSettingRequest;
import com.pos.dto.AppSettingResponse;
import com.pos.exception.ResourceNotFoundException;
import com.pos.model.AppSetting;
import com.pos.repository.AppSettingRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppSettingServiceImpl implements AppSettingService {

    private final AppSettingRepository repo;

    @Override
    @Transactional(readOnly = true)
    public List<AppSettingResponse> findAll() {
        return repo.findAll().stream()
                .map(s -> new AppSettingResponse(s.getKey(), s.getValue(), s.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppSettingResponse findByKey(String key) {
        AppSetting s = getOrThrow(key);
        return new AppSettingResponse(s.getKey(), s.getValue(), s.getDescription());
    }

    @Override
    public AppSettingResponse update(String key, AppSettingRequest request) {
        AppSetting s = getOrThrow(key);
        s.setValue(request.value());
        return new AppSettingResponse(s.getKey(), s.getValue(), s.getDescription());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        return repo.findById(key)
                .map(AppSetting::getValue)
                .filter(v -> v != null && !v.isBlank())
                .map(v -> { try { return new BigDecimal(v); } catch (NumberFormatException e) { return defaultValue; } })
                .orElse(defaultValue);
    }

    private AppSetting getOrThrow(String key) {
        return repo.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting '" + key + "' tidak ditemukan"));
    }
}
