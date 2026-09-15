package com.pos.user.service;

import com.pos.common.exception.BadRequestException;
import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.user.dto.UserRequest;
import com.pos.user.dto.UserResponse;
import com.pos.user.entity.User;
import com.pos.user.mapper.UserMapper;
import com.pos.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return userRepository.findByFullNameContainingIgnoreCase(keyword, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userMapper.toResponse(getOrThrow(id));
    }

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' sudah digunakan");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password wajib diisi saat membuat user baru");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole())
                .active(request.isActive())
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse update(UUID id, UserRequest request) {
        User user = getOrThrow(id);
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' sudah digunakan");
        }
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(request.isActive());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(UUID id) {
        User user = getOrThrow(id);
        userRepository.delete(user);
    }

    private User getOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
