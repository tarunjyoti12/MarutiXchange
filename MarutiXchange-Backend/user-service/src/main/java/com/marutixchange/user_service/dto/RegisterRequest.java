package com.marutixchange.user_service.dto;

import com.marutixchange.user_service.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private User.Role role;
}