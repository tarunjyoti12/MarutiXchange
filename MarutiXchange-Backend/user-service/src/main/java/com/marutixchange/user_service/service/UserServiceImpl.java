package com.marutixchange.user_service.service;

import com.marutixchange.user_service.dto.LoginRequest;
import com.marutixchange.user_service.dto.LoginResponse;
import com.marutixchange.user_service.dto.RegisterRequest;
import com.marutixchange.user_service.dto.UpdateUserRequest;
import com.marutixchange.user_service.dto.UserResponse;
import com.marutixchange.user_service.entity.User;
import com.marutixchange.user_service.exception.AccountLockedException;
import com.marutixchange.user_service.exception.EmailAlreadyExistsException;
import com.marutixchange.user_service.exception.InvalidCredentialsException;
import com.marutixchange.user_service.exception.InvalidOtpException;
import com.marutixchange.user_service.exception.InvalidTokenException;
import com.marutixchange.user_service.exception.UserNotFoundException;
import com.marutixchange.user_service.repository.UserRepository;
import com.marutixchange.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .active(true)
                .isVerified(false)
                .failedAttempts(0)
                .accountLocked(false)
                .build();

        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
        log.info("Registered id: {}", savedUser.getId());
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            if (user.getLockTime() != null
                    && user.getLockTime()
                    .plusMinutes(LOCK_DURATION_MINUTES)
                    .isAfter(LocalDateTime.now())) {
                throw new AccountLockedException();
            } else {
                resetFailedAttempts(user);
            }
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException();
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new InvalidCredentialsException();
        }

        resetFailedAttempts(user);

        // FIX: Pass role as third argument so it is embedded in the JWT token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()   // ✅ FIXED: role now in token
        );

        log.info("Login successful: {}", user.getId());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Fetching user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        User updated = userRepository.save(user);
        log.info("User updated: {}", id);
        return UserResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User soft-deleted: {}", id);
    }

    @Override
    @Transactional
    public void sendOtp(String email) {
        log.info("Sending OTP to: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);
        emailService.sendOtpEmail(email, otp);
        log.info("OTP sent to: {}", email);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        log.info("Verifying OTP for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.getOtp() == null
                || user.getOtpExpiry() == null
                || LocalDateTime.now().isAfter(user.getOtpExpiry())
                || !user.getOtp().equals(otp)) {
            throw new InvalidOtpException();
        }

        user.setIsVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        log.info("OTP verified for: {}", email);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("Forgot password: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, token);
        log.info("Reset email sent: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password");
        User user = userRepository.findByResetToken(token)
                .orElseThrow(InvalidTokenException::new);

        if (user.getResetTokenExpiry() == null
                || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new InvalidTokenException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        log.info("Password reset successful");
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    private void handleFailedLogin(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
            log.warn("Account locked: {}", user.getEmail());
        }
        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
