package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.client.RuleEngineClient;
import com.marutixchange.car_listing_service.dto.CarListingRequest;
import com.marutixchange.car_listing_service.dto.CarListingResponse;
import com.marutixchange.car_listing_service.dto.RuleContext;
import com.marutixchange.car_listing_service.entity.CarListing;
import com.marutixchange.car_listing_service.exception.CarListingNotFoundException;
import com.marutixchange.car_listing_service.repository.CarImageRepository;
import com.marutixchange.car_listing_service.repository.CarListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarListingServiceImplTest {

    @Mock
    private CarListingRepository listingRepository;

    @Mock
    private CarImageRepository imageRepository;

    @Mock
    private RuleEngineClient ruleEngineClient; // ✅ FIX ADDED

    @InjectMocks
    private CarListingServiceImpl listingService;

    private CarListing mockListing;
    private CarListingRequest listingRequest;

    @BeforeEach
    void setUp() {

        mockListing = CarListing.builder()
                .id(1L)
                .sellerId(1L)
                .brand("Maruti")
                .model("Swift")
                .year(2022)
                .fuelType(CarListing.FuelType.PETROL)
                .transmission(CarListing.Transmission.MANUAL)
                .askingPrice(650000.0)
                .status(CarListing.ListingStatus.ACTIVE)
                .viewCount(0)
                .inquiryCount(0)
                .isCertified(false)
                .isBoosted(false)
                .testDriveAvailable(true)
                .build();

        listingRequest = new CarListingRequest();
        listingRequest.setSellerId(1L);
        listingRequest.setBrand("Maruti");
        listingRequest.setModel("Swift");
        listingRequest.setYear(2022);
        listingRequest.setFuelType(CarListing.FuelType.PETROL);
        listingRequest.setTransmission(CarListing.Transmission.MANUAL);
        listingRequest.setAskingPrice(650000.0);
    }

    // ✅ CREATE TEST
    @Test
    void createListing_shouldReturnResponse() {

        // 🔥 MOCK RULE ENGINE RESPONSE
        RuleContext mockResponse = new RuleContext();
        mockResponse.setApproved(true);
        mockResponse.setRuleMatched(true); // ✅ FIX

        when(ruleEngineClient.evaluateRules(any()))
                .thenReturn(mockResponse);

        // 🔥 MOCK DB SAVE
        when(listingRepository.save(any()))
                .thenReturn(mockListing);

        CarListingResponse response =
                listingService.createListing(listingRequest);

        assertNotNull(response);
        assertEquals("Maruti", response.getBrand());

        verify(listingRepository, times(1)).save(any());
    }

    // ✅ GET SUCCESS
    @Test
    void getListingById_shouldReturnListing() {

        when(listingRepository.findById(1L))
                .thenReturn(Optional.of(mockListing));

        CarListingResponse response =
                listingService.getListingById(1L);

        assertNotNull(response);

        verify(listingRepository, times(2)).findById(1L);
    }

    // ✅ GET NOT FOUND
    @Test
    void getListingById_shouldThrow_whenNotFound() {

        when(listingRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CarListingNotFoundException.class,
                () -> listingService.getListingById(99L));
    }

    // ✅ MARK SOLD
    @Test
    void markAsSold_shouldUpdateStatus() {

        when(listingRepository.findById(1L))
                .thenReturn(Optional.of(mockListing));

        listingService.markAsSold(1L);

        assertEquals(
                CarListing.ListingStatus.SOLD,
                mockListing.getStatus());

        verify(listingRepository).save(any());
    }

    // ✅ DELETE
    @Test
    void deleteListing_shouldExpireListing() {

        when(listingRepository.findById(1L))
                .thenReturn(Optional.of(mockListing));

        listingService.deleteListing(1L);

        assertEquals(
                CarListing.ListingStatus.EXPIRED,
                mockListing.getStatus());

        verify(listingRepository).save(any());
    }
}