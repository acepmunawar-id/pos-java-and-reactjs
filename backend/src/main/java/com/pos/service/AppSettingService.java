package com.pos.service;

import com.pos.dto.AppSettingRequest;
import com.pos.dto.AppSettingResponse;
import java.math.BigDecimal;
import java.util.List;

public interface AppSettingService {
    List<AppSettingResponse> findAll();
    AppSettingResponse findByKey(String key);
    AppSettingResponse update(String key, AppSettingRequest request);
    BigDecimal getDecimal(String key, BigDecimal defaultValue);
}
