package com.marutixchange.user_service.service;

import com.marutixchange.user_service.dto.*;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void sendOtp(String email);

    void verifyOtp(String email, String otp);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    // ✅ FIXED
    Page<UserResponse> getAllUsers(
            int page,
            int size,
            String sortBy,
            String direction);
}