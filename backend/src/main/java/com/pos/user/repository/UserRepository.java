package com.pos.user.repository;

import com.pos.security.Role;
import com.pos.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    Page<User> findByRoleAndFullNameContainingIgnoreCase(Role role, String fullName, Pageable pageable);

    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
}
