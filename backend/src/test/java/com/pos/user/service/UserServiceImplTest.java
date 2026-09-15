package com.pos.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.BadRequestException;
import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.security.Role;
import com.pos.user.dto.UserRequest;
import com.pos.user.dto.UserResponse;
import com.pos.user.entity.User;
import com.pos.user.mapper.UserMapper;
import com.pos.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest request;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);

        request = new UserRequest();
        request.setUsername("newadmin");
        request.setPassword("Password123!");
        request.setFullName("New Admin");
        request.setEmail("newadmin@pos.local");
        request.setRole(Role.ADMIN);
        request.setActive(true);
    }

    @Test
    void create_savesEncodedPassword_whenUsernameNotTaken() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.create(request);

        assertThat(response.getUsername()).isEqualTo("newadmin");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void create_throwsDuplicateResourceException_whenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_throwsBadRequest_whenPasswordBlank() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        request.setPassword(" ");

        assertThatThrownBy(() -> userService.create(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void findById_throwsResourceNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_keepsExistingPassword_whenPasswordFieldLeftBlank() {
        UUID id = UUID.randomUUID();
        User existing = User.builder()
                .id(id)
                .username("newadmin")
                .password("OLD_HASH")
                .fullName("Old Name")
                .role(Role.ADMIN)
                .active(true)
                .build();
        request.setPassword(null);
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsernameAndIdNot("newadmin", id)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.update(id, request);

        assertThat(existing.getPassword()).isEqualTo("OLD_HASH");
    }
}
