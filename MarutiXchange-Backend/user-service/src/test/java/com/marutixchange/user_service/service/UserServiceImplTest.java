package com.marutixchange.user_service.service;

import com.marutixchange.user_service.dto.*;
import com.marutixchange.user_service.entity.User;
import com.marutixchange.user_service.exception.*;
import com.marutixchange.user_service.repository.UserRepository;
import com.marutixchange.user_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    // ================= CONSTANTS =================
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "securePass123";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String MOCK_TOKEN = "mockToken";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        mockUser = User.builder()
                .id(1L)
                .name("Test User")
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .role(User.Role.BUYER)
                .active(true)
                .isVerified(false)
                .failedAttempts(0)
                .accountLocked(false)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setRole(User.Role.BUYER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);
    }

    // ================= REGISTER =================

    @Test
    void shouldRegisterUser_whenValidRequest() {

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals("Test User", response.getName());

        verify(emailService).sendWelcomeEmail(TEST_EMAIL, "Test User");
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists() {

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.register(registerRequest));
    }

    // ================= LOGIN =================

    @Test
    void shouldReturnJwtToken_whenCredentialsAreValid() {

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        // FIX: generateToken now takes 3 args (Long, String, String role)
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString()))
                .thenReturn(MOCK_TOKEN);

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.getToken());
        assertEquals("Bearer", response.getTokenType());

        // Verify role is passed correctly
        verify(jwtUtil).generateToken(
                eq(mockUser.getId()),
                eq(TEST_EMAIL),
                eq("BUYER")
        );
    }

    @Test
    void shouldThrowException_whenUserNotFound() {

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(loginRequest));
    }

    @Test
    void shouldThrowException_whenPasswordIsIncorrect() {

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(loginRequest));
    }

    @Test
    void shouldThrowException_whenAccountIsLocked() {

        mockUser.setAccountLocked(true);
        mockUser.setLockTime(LocalDateTime.now().minusMinutes(5));

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));

        assertThrows(AccountLockedException.class,
                () -> userService.login(loginRequest));
    }

    // ================= OTP =================

    @Test
    void shouldGenerateOtpAndSendEmail() {

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));

        userService.sendOtp(TEST_EMAIL);

        assertNotNull(mockUser.getOtp());
        verify(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    void shouldVerifyOtp_whenValidOtp() {

        mockUser.setOtp("123456");
        mockUser.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));

        userService.verifyOtp(TEST_EMAIL, "123456");

        assertTrue(mockUser.getIsVerified());
    }

    @Test
    void shouldThrowException_whenOtpIsInvalidOrExpired() {

        mockUser.setOtp("123456");
        mockUser.setOtpExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(mockUser));

        assertThrows(InvalidOtpException.class,
                () -> userService.verifyOtp(TEST_EMAIL, "123456"));
    }

    // ================= PASSWORD RESET =================

    @Test
    void shouldResetPassword_whenTokenIsValid() {

        mockUser.setResetToken("token123");
        mockUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByResetToken("token123"))
                .thenReturn(Optional.of(mockUser));

        when(passwordEncoder.encode("newPass"))
                .thenReturn("newEncodedPass");

        userService.resetPassword("token123", "newPass");

        verify(userRepository).save(mockUser);
    }

    @Test
    void shouldThrowException_whenResetTokenInvalid() {

        when(userRepository.findByResetToken("token123"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class,
                () -> userService.resetPassword("token123", "newPass"));
    }

    // ================= USER =================

    @Test
    void shouldReturnUser_whenUserExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        UserResponse response = userService.getUserById(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    void shouldUpdateUser_whenValidRequest() {

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        when(userRepository.save(any(User.class)))
                .thenReturn(mockUser);

        userService.updateUser(1L, request);

        verify(userRepository).save(mockUser);
    }

    @Test
    void shouldSoftDeleteUser_whenUserExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        userService.deleteUser(1L);

        assertFalse(mockUser.getActive());
    }
}