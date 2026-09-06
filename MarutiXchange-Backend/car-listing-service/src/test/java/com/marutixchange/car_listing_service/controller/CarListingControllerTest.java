package com.marutixchange.car_listing_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marutixchange.car_listing_service.dto.CarListingRequest;
import com.marutixchange.car_listing_service.dto.CarListingResponse;
import com.marutixchange.car_listing_service.entity.CarListing;
import com.marutixchange.car_listing_service.security.JwtAuthFilter;
import com.marutixchange.car_listing_service.security.JwtUtil;
import com.marutixchange.car_listing_service.service.CarListingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CarListingController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class CarListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarListingService listingService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void createListing_shouldReturn201() throws Exception {

        CarListingRequest request = new CarListingRequest();
        request.setSellerId(1L);
        request.setBrand("Maruti");
        request.setModel("Swift");
        request.setYear(2022);
        request.setFuelType(CarListing.FuelType.PETROL);
        request.setTransmission(CarListing.Transmission.MANUAL);
        request.setAskingPrice(650000.0);

        CarListingResponse mockResponse =
                CarListingResponse.builder()
                        .id(1L)
                        .sellerId(1L)
                        .brand("Maruti")
                        .model("Swift")
                        .year(2022)
                        .fuelType("PETROL")
                        .transmission("MANUAL")
                        .askingPrice(650000.0)
                        .status("ACTIVE")
                        .build();

        when(listingService.createListing(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(
                        post("/api/v1/listings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.brand").value("Maruti"));
    }
}