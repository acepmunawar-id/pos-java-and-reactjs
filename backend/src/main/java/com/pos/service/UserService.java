package com.pos.service;

import com.pos.dto.UserRequest;
import com.pos.dto.UserResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> findAll(String search, Pageable pageable);

    UserResponse findById(UUID id);

    UserResponse create(UserRequest request);

    UserResponse update(UUID id, UserRequest request);

    void delete(UUID id);
}
