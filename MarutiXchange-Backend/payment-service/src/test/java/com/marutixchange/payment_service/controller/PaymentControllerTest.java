package com.marutixchange.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marutixchange.payment_service.dto.PaymentRequest;
import com.marutixchange.payment_service.dto.PaymentResponse;
import com.marutixchange.payment_service.security.JwtAuthFilter;
import com.marutixchange.payment_service.security.JwtUtil;
import com.marutixchange.payment_service.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void initiatePayment_shouldReturn201() throws Exception {

        // ✅ REQUEST
        PaymentRequest request = PaymentRequest.builder()
                .carListingId(1L)
                .buyerId(3L)
                .sellerId(2L)
                .amount(10000.0)
                .paymentMethod("UPI")
                .paymentType("TOKEN")
                .upiId("buyer@gpay")
                .isTokenPayment(true)
                .build();

        // ✅ MOCK RESPONSE (CRITICAL FIX)
        PaymentResponse mockResponse =
                PaymentResponse.builder()
                        .id(1L)
                        .transactionId("MM123456789ABC")
                        .paymentId("MM123456789ABC") // 🔥 VERY IMPORTANT FIX
                        .carListingId(1L)
                        .buyerId(3L)
                        .sellerId(2L)
                        .amount(10000.0)
                        .status("PENDING")
                        .paymentMethod("UPI")
                        .paymentType("TOKEN")
                        .invoiceNumber("INV-MX-123456")
                        .isTokenPayment(true)
                        .build();

        when(paymentService.initiatePayment(any(PaymentRequest.class)))
                .thenReturn(mockResponse);

        // ✅ TEST
        mockMvc.perform(
                        post("/api/v1/payments/initiate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value("MM123456789ABC"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentMethod").value("UPI"));
    }
}