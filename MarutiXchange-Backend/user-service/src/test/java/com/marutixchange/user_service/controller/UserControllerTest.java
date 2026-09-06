package com.marutixchange.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marutixchange.user_service.dto.RegisterRequest;
import com.marutixchange.user_service.dto.UserResponse;
import com.marutixchange.user_service.entity.User;
import com.marutixchange.user_service.security.JwtAuthFilter;
import com.marutixchange.user_service.security.JwtUtil;
import com.marutixchange.user_service.service.DocumentService;
import com.marutixchange.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_shouldReturn201_whenValidRequest() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Arjun Sharma");
        request.setEmail("arjun@marutixchange.com");
        request.setPassword("securePass123");
        request.setRole(User.Role.BUYER);

        UserResponse mockResponse = UserResponse.builder()
                .id(1L)
                .name("Arjun Sharma")
                .email("arjun@marutixchange.com")
                .role("BUYER")
                .active(true)
                .isVerified(false)
                .accountLocked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.register(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .with(user("testUser").roles("BUYER")) // 🔥 FIX
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email")
                        .value("arjun@marutixchange.com"));
    }

    @Test
    void register_shouldReturn400_whenInvalidEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setRole(User.Role.BUYER);

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .with(user("testUser").roles("BUYER")) // 🔥 FIX
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}