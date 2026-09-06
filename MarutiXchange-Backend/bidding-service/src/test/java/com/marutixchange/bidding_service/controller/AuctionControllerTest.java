package com.marutixchange.bidding_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marutixchange.bidding_service.dto.AuctionRequest;
import com.marutixchange.bidding_service.dto.AuctionResponse;
import com.marutixchange.bidding_service.security.JwtAuthFilter;
import com.marutixchange.bidding_service.security.JwtUtil;
import com.marutixchange.bidding_service.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuctionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class AuctionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AuctionService auctionService;
    @MockBean private JwtUtil jwtUtil;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createAuction_shouldReturn201() throws Exception {

        AuctionRequest request = new AuctionRequest();
        request.setCarListingId(1L);
        request.setCarName("Maruti Swift 2022");
        request.setStartingPrice(500000.0);
        request.setMinBidIncrement(5000.0);
        request.setStartTime(LocalDateTime.now().plusMinutes(5));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        AuctionResponse mockResponse = AuctionResponse.builder()
                .id(1L)
                .carListingId(1L)
                .sellerId(1L)
                .carName("Maruti Swift 2022")
                .startingPrice(500000.0)
                .currentHighestBid(0.0)
                .totalBids(0)
                .status("SCHEDULED")
                .build();

        when(auctionService.createAuction(any(AuctionRequest.class), anyLong()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auctions")
                        .requestAttr("userId", 1L) // 🔥 IMPORTANT
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.carName").value("Maruti Swift 2022"));
    }
}