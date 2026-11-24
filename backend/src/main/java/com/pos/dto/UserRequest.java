package com.pos.dto;

import com.pos.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Username wajib diisi")
    @Size(min = 3, max = 50, message = "Username harus 3-50 karakter")
    private String username;

    /** Wajib diisi saat membuat user baru; kosongkan saat update jika tidak ingin mengganti password. */
    @Size(min = 6, message = "Password minimal 6 karakter")
    private String password;

    @NotBlank(message = "Nama lengkap wajib diisi")
    private String fullName;

    @Email(message = "Format email tidak valid")
    private String email;

    @NotNull(message = "Role wajib dipilih")
    private Role role;

    private boolean active = true;
}
