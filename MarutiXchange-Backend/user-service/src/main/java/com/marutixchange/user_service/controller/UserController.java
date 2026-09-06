package com.marutixchange.user_service.controller;

import com.marutixchange.user_service.dto.*;
import com.marutixchange.user_service.entity.UserDocument;
import com.marutixchange.user_service.service.DocumentService;
import com.marutixchange.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final DocumentService documentService;

    // ================= AUTH =================

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request for email: {}", request.getEmail());

        UserResponse response = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}", request.getEmail());

        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    // ================= USER =================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id) {

        log.info("Fetching user with id: {}", id);

        UserResponse response = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user with id: {}", id);

        UserResponse response = userService.updateUser(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id) {

        log.info("Deleting user with id: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Fetching users: page={}, size={}", page, size);

        Page<UserResponse> users =
                userService.getAllUsers(page, size, sortBy, direction);

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users));
    }

    // ================= OTP =================

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody OtpRequest request) {

        log.info("Sending OTP to email: {}", request.getEmail());

        userService.sendOtp(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {

        log.info("Verifying OTP for email: {}", request.getEmail());

        userService.verifyOtp(request.getEmail(), request.getOtp());

        return ResponseEntity.ok(
                ApiResponse.success("OTP verified successfully", null));
    }

    // ================= PASSWORD =================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        log.info("Forgot password for email: {}", request.getEmail());

        userService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Password reset link sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request) {

        log.info("Reset password request received");

        userService.resetPassword(
                request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successful", null));
    }

    // ================= DOCUMENT =================

    @PostMapping("/{id}/upload-document")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType")
            UserDocument.DocumentType documentType) {

        log.info("Uploading document for user id: {}", id);

        if (file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        DocumentResponse response =
                documentService.uploadDocument(id, file, documentType);

        return ResponseEntity.ok(
                ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getUserDocuments(
            @PathVariable Long id) {

        log.info("Fetching documents for user id: {}", id);

        List<DocumentResponse> documents =
                documentService.getUserDocuments(id);

        return ResponseEntity.ok(
                ApiResponse.success("Documents fetched successfully", documents));
    }
}