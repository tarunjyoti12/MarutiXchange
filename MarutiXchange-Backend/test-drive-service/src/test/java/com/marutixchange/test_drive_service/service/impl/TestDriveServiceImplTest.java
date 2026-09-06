package com.marutixchange.test_drive_service.service.impl;

import com.marutixchange.test_drive_service.client.CarListingFeignClient;
import com.marutixchange.test_drive_service.dto.*;
import com.marutixchange.test_drive_service.entity.TestDrive;
import com.marutixchange.test_drive_service.exception.InvalidOperationException;
import com.marutixchange.test_drive_service.exception.SlotNotAvailableException;
import com.marutixchange.test_drive_service.exception.TestDriveNotFoundException;
import com.marutixchange.test_drive_service.repository.TestDriveRepository;
import com.marutixchange.test_drive_service.repository.TestDriveSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestDriveServiceImpl Unit Tests")
class TestDriveServiceImplTest {

    @Mock private TestDriveRepository testDriveRepository;
    @Mock private TestDriveSlotRepository slotRepository;
    @Mock private CarListingFeignClient carListingFeignClient;

    @InjectMocks
    private TestDriveServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "maxActiveBookingsPerUser", 3);
        ReflectionTestUtils.setField(service, "maxAdvanceBookingDays", 30);
        ReflectionTestUtils.setField(service, "cancellationAllowedHours", 2);
    }

    // ─── bookTestDrive ────────────────────────────────────────────────────────

    @Test
    @DisplayName("bookTestDrive - success")
    void bookTestDrive_success() {
        TestDriveRequest req = buildRequest(LocalDate.now().plusDays(3),
                LocalTime.of(10, 0));

        when(testDriveRepository.countActiveBookingsByBuyer(1L)).thenReturn(0L);
        when(testDriveRepository.existsConflictingBooking(any(), any(), any())).thenReturn(false);
        when(carListingFeignClient.getListingById(10L)).thenReturn(buildListingResponse());
        when(slotRepository.findByListingIdAndSlotDateAndSlotTime(any(), any(), any()))
                .thenReturn(Optional.empty());

        TestDrive saved = buildTestDrive(req);
        when(testDriveRepository.save(any())).thenReturn(saved);

        TestDriveResponse result = service.bookTestDrive(req);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(testDriveRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("bookTestDrive - exceeds advance booking window")
    void bookTestDrive_exceedsAdvanceWindow() {
        TestDriveRequest req = buildRequest(LocalDate.now().plusDays(40),
                LocalTime.of(10, 0));

        assertThatThrownBy(() -> service.bookTestDrive(req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("30 days in advance");
    }

    @Test
    @DisplayName("bookTestDrive - too many active bookings")
    void bookTestDrive_tooManyActive() {
        TestDriveRequest req = buildRequest(LocalDate.now().plusDays(3),
                LocalTime.of(10, 0));

        when(testDriveRepository.countActiveBookingsByBuyer(1L)).thenReturn(3L);

        assertThatThrownBy(() -> service.bookTestDrive(req))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("3 active bookings");
    }

    @Test
    @DisplayName("bookTestDrive - slot conflict")
    void bookTestDrive_slotConflict() {
        TestDriveRequest req = buildRequest(LocalDate.now().plusDays(3),
                LocalTime.of(10, 0));

        when(testDriveRepository.countActiveBookingsByBuyer(1L)).thenReturn(0L);
        when(testDriveRepository.existsConflictingBooking(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.bookTestDrive(req))
                .isInstanceOf(SlotNotAvailableException.class);
    }

    // ─── confirmTestDrive ─────────────────────────────────────────────────────

    @Test
    @DisplayName("confirmTestDrive - success from PENDING")
    void confirmTestDrive_success() {
        TestDrive td = buildTestDrive(null);
        td.setStatus(TestDrive.TestDriveStatus.PENDING);

        when(testDriveRepository.findById(1L)).thenReturn(Optional.of(td));
        when(testDriveRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TestDriveResponse result = service.confirmTestDrive(1L);
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("confirmTestDrive - fails when already CONFIRMED")
    void confirmTestDrive_alreadyConfirmed() {
        TestDrive td = buildTestDrive(null);
        td.setStatus(TestDrive.TestDriveStatus.CONFIRMED);

        when(testDriveRepository.findById(1L)).thenReturn(Optional.of(td));

        assertThatThrownBy(() -> service.confirmTestDrive(1L))
                .isInstanceOf(InvalidOperationException.class);
    }

    // ─── getById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById - not found throws exception")
    void getById_notFound() {
        when(testDriveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(TestDriveNotFoundException.class);
    }

    // ─── submitFeedback ───────────────────────────────────────────────────────

    @Test
    @DisplayName("submitFeedback - fails if not COMPLETED")
    void submitFeedback_notCompleted() {
        TestDrive td = buildTestDrive(null);
        td.setStatus(TestDrive.TestDriveStatus.CONFIRMED);

        when(testDriveRepository.findById(1L)).thenReturn(Optional.of(td));

        assertThatThrownBy(() -> service.submitFeedback(1L,
                new FeedbackRequest()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("submitFeedback - fails if feedback already given")
    void submitFeedback_alreadyGiven() {
        TestDrive td = buildTestDrive(null);
        td.setStatus(TestDrive.TestDriveStatus.COMPLETED);
        td.setBuyerRating(4);

        when(testDriveRepository.findById(1L)).thenReturn(Optional.of(td));

        assertThatThrownBy(() -> service.submitFeedback(1L, new FeedbackRequest()))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already submitted");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TestDriveRequest buildRequest(LocalDate date, LocalTime time) {
        TestDriveRequest r = new TestDriveRequest();
        r.setBuyerId(1L);
        r.setListingId(10L);
        r.setSlotDate(date);
        r.setSlotTime(time);
        r.setLocationCity("Delhi");
        return r;
    }

    private TestDrive buildTestDrive(TestDriveRequest req) {
        return TestDrive.builder()
                .id(1L)
                .buyerId(req != null ? req.getBuyerId() : 1L)
                .listingId(req != null ? req.getListingId() : 10L)
                .sellerId(5L)
                .slotDate(req != null ? req.getSlotDate() : LocalDate.now().plusDays(3))
                .slotTime(req != null ? req.getSlotTime() : LocalTime.of(10, 0))
                .status(TestDrive.TestDriveStatus.PENDING)
                .build();
    }

    private CarListingClientResponse buildListingResponse() {
        CarListingClientResponse.ListingData data = new CarListingClientResponse.ListingData();
        data.setId(10L);
        data.setSellerId(5L);
        data.setStatus("ACTIVE");
        data.setTestDriveAvailable(true);

        CarListingClientResponse response = new CarListingClientResponse();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}
