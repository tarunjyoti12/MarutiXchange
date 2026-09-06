package com.marutixchange.test_drive_service.service.impl;

import com.marutixchange.test_drive_service.client.CarListingFeignClient;
import com.marutixchange.test_drive_service.client.NotificationFeignClient;
import com.marutixchange.test_drive_service.dto.*;
import com.marutixchange.test_drive_service.entity.TestDrive;
import com.marutixchange.test_drive_service.entity.TestDriveSlot;
import com.marutixchange.test_drive_service.exception.InvalidOperationException;
import com.marutixchange.test_drive_service.exception.SlotNotAvailableException;
import com.marutixchange.test_drive_service.exception.TestDriveNotFoundException;
import com.marutixchange.test_drive_service.repository.TestDriveRepository;
import com.marutixchange.test_drive_service.repository.TestDriveSlotRepository;
import com.marutixchange.test_drive_service.service.TestDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDriveServiceImpl implements TestDriveService {

    private final TestDriveRepository     testDriveRepository;
    private final TestDriveSlotRepository slotRepository;
    private final CarListingFeignClient   carListingFeignClient;
    private final NotificationFeignClient notificationFeignClient;

    @Value("${app.testdrive.max-active-bookings-per-user:3}")
    private int maxActiveBookingsPerUser;

    @Value("${app.testdrive.max-advance-booking-days:30}")
    private int maxAdvanceBookingDays;

    @Value("${app.testdrive.cancellation-allowed-hours:2}")
    private int cancellationAllowedHours;

    // ─── Book ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse bookTestDrive(TestDriveRequest request) {

        log.info("Booking test drive: buyerId={}, listingId={}", request.getBuyerId(), request.getListingId());

        // Validate advance booking window
        LocalDate maxDate = LocalDate.now().plusDays(maxAdvanceBookingDays);
        if (request.getSlotDate().isAfter(maxDate)) {
            throw new InvalidOperationException(
                    "Cannot book more than " + maxAdvanceBookingDays + " days in advance.");
        }

        // Check buyer active booking limit
        long activeCount = testDriveRepository.countActiveBookingsByBuyer(request.getBuyerId());
        if (activeCount >= maxActiveBookingsPerUser) {
            throw new InvalidOperationException(
                    "You already have " + maxActiveBookingsPerUser + " active bookings.");
        }

        // Check for conflicting booking on same listing + slot
        boolean conflict = testDriveRepository.existsConflictingBooking(
                request.getListingId(), request.getSlotDate(), request.getSlotTime());
        if (conflict) {
            throw new SlotNotAvailableException("This slot is already booked. Please choose another time.");
        }

        // Fetch listing details from car-listing-service
        Long sellerId = fetchSellerIdFromListing(request.getListingId());

        // Check if pre-defined slot exists
        Optional<TestDriveSlot> slotOpt = slotRepository
                .findByListingIdAndSlotDateAndSlotTime(
                        request.getListingId(), request.getSlotDate(), request.getSlotTime());

        // Build and save test drive
        TestDrive testDrive = TestDrive.builder()
                .buyerId(request.getBuyerId())
                .listingId(request.getListingId())
                .sellerId(sellerId)
                .slotDate(request.getSlotDate())
                .slotTime(request.getSlotTime())
                .locationAddress(request.getLocationAddress())
                .locationCity(request.getLocationCity())
                .locationPincode(request.getLocationPincode())
                .status(TestDrive.TestDriveStatus.PENDING)
                .build();

        TestDrive saved = testDriveRepository.save(testDrive);

        // Mark slot as booked
        slotOpt.ifPresent(slot -> {
            slot.setIsBooked(true);
            slot.setBookedByTestDriveId(saved.getId());
            slotRepository.save(slot);
        });

        // Notify seller about new test drive request
        if (sellerId != null) {
            sendNotification(sellerId, "TEST_DRIVE_REQUESTED",
                    "New Test Drive Request",
                    "A buyer has requested a test drive on "
                            + request.getSlotDate() + " at " + request.getSlotTime());
        }

        // Notify buyer that booking is received
        sendNotification(request.getBuyerId(), "TEST_DRIVE_REQUESTED",
                "Test Drive Requested",
                "Your test drive request has been submitted for "
                        + request.getSlotDate() + " at " + request.getSlotTime()
                        + ". Waiting for seller confirmation.");

        log.info("Test drive booked: id={}", saved.getId());
        return TestDriveResponse.fromEntity(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public TestDriveResponse getById(Long id) {
        return TestDriveResponse.fromEntity(findOrThrow(id));
    }

    @Override
    public Page<TestDriveResponse> getByBuyer(Long buyerId, int page, int size) {
        return testDriveRepository
                .findByBuyerId(buyerId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(TestDriveResponse::fromEntity);
    }

    @Override
    public Page<TestDriveResponse> getBySeller(Long sellerId, int page, int size) {
        return testDriveRepository
                .findBySellerId(sellerId,
                        PageRequest.of(page, size, Sort.by("slotDate").ascending()))
                .map(TestDriveResponse::fromEntity);
    }

    @Override
    public List<TestDriveResponse> getByListing(Long listingId) {
        return testDriveRepository.findByListingId(listingId)
                .stream().map(TestDriveResponse::fromEntity).toList();
    }

    // ─── Confirm ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse confirmTestDrive(Long id) {
        TestDrive td = findOrThrow(id);

        if (td.getStatus() != TestDrive.TestDriveStatus.PENDING) {
            throw new InvalidOperationException(
                    "Only PENDING test drives can be confirmed. Current: " + td.getStatus());
        }

        td.setStatus(TestDrive.TestDriveStatus.CONFIRMED);
        TestDrive saved = testDriveRepository.save(td);

        // Notify buyer test drive is confirmed
        sendNotification(td.getBuyerId(), "TEST_DRIVE_CONFIRMED",
                "Test Drive Confirmed!",
                "Your test drive on " + td.getSlotDate()
                        + " at " + td.getSlotTime() + " has been confirmed by the seller.");

        return TestDriveResponse.fromEntity(saved);
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse cancelTestDrive(Long id, CancelRequest cancelRequest) {
        TestDrive td = findOrThrow(id);

        if (td.getStatus() == TestDrive.TestDriveStatus.COMPLETED
                || td.getStatus() == TestDrive.TestDriveStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot cancel a " + td.getStatus() + " test drive.");
        }

        // Enforce cancellation window
        LocalDateTime slotDateTime = td.getSlotDate().atTime(td.getSlotTime());
        LocalDateTime cutoff = slotDateTime.minusHours(cancellationAllowedHours);
        if (LocalDateTime.now().isAfter(cutoff)) {
            throw new InvalidOperationException(
                    "Cancellation not allowed within " + cancellationAllowedHours
                            + " hours of the test drive.");
        }

        td.setStatus(TestDrive.TestDriveStatus.CANCELLED);
        td.setCancelledBy(cancelRequest.getCancelledBy());
        td.setCancellationReason(cancelRequest.getCancellationReason());
        td.setCancelledAt(LocalDateTime.now());

        releaseSlot(td.getListingId(), td.getId());

        TestDrive saved = testDriveRepository.save(td);

        // Notify both parties
        sendNotification(td.getBuyerId(), "TEST_DRIVE_CANCELLED",
                "Test Drive Cancelled",
                "Your test drive on " + td.getSlotDate() + " has been cancelled.");

        if (td.getSellerId() != null) {
            sendNotification(td.getSellerId(), "TEST_DRIVE_CANCELLED",
                    "Test Drive Cancelled",
                    "A test drive scheduled for " + td.getSlotDate() + " has been cancelled.");
        }

        return TestDriveResponse.fromEntity(saved);
    }

    // ─── Complete ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse completeTestDrive(Long id) {
        TestDrive td = findOrThrow(id);

        if (td.getStatus() != TestDrive.TestDriveStatus.CONFIRMED) {
            throw new InvalidOperationException(
                    "Only CONFIRMED test drives can be completed. Current: " + td.getStatus());
        }

        td.setStatus(TestDrive.TestDriveStatus.COMPLETED);
        td.setCompletedAt(LocalDateTime.now());

        TestDrive saved = testDriveRepository.save(td);

        // Prompt buyer to submit feedback
        sendNotification(td.getBuyerId(), "TEST_DRIVE_COMPLETED",
                "Test Drive Completed!",
                "How was your test drive? Please rate your experience.");

        return TestDriveResponse.fromEntity(saved);
    }

    // ─── No Show ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse markNoShow(Long id) {
        TestDrive td = findOrThrow(id);

        if (td.getStatus() != TestDrive.TestDriveStatus.CONFIRMED) {
            throw new InvalidOperationException("Only CONFIRMED test drives can be marked as no-show.");
        }

        td.setStatus(TestDrive.TestDriveStatus.NO_SHOW);
        releaseSlot(td.getListingId(), td.getId());

        TestDrive saved = testDriveRepository.save(td);

        // Notify seller
        if (td.getSellerId() != null) {
            sendNotification(td.getSellerId(), "TEST_DRIVE_NO_SHOW",
                    "Buyer Did Not Show",
                    "The buyer did not show up for the test drive on " + td.getSlotDate() + ".");
        }

        return TestDriveResponse.fromEntity(saved);
    }

    // ─── Feedback ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse submitFeedback(Long id, FeedbackRequest feedbackRequest) {
        TestDrive td = findOrThrow(id);

        if (td.getStatus() != TestDrive.TestDriveStatus.COMPLETED) {
            throw new InvalidOperationException("Feedback can only be submitted after test drive is COMPLETED.");
        }
        if (td.getBuyerRating() != null) {
            throw new InvalidOperationException("Feedback already submitted.");
        }

        td.setBuyerRating(feedbackRequest.getBuyerRating());
        td.setBuyerFeedback(feedbackRequest.getBuyerFeedback());
        return TestDriveResponse.fromEntity(testDriveRepository.save(td));
    }

    // ─── Seller Notes ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TestDriveResponse addSellerNotes(Long id, String notes) {
        TestDrive td = findOrThrow(id);
        td.setSellerNotes(notes);
        return TestDriveResponse.fromEntity(testDriveRepository.save(td));
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private TestDrive findOrThrow(Long id) {
        return testDriveRepository.findById(id)
                .orElseThrow(() -> new TestDriveNotFoundException(id));
    }

    private void releaseSlot(Long listingId, Long testDriveId) {
        slotRepository.findAll().stream()
                .filter(s -> s.getListingId().equals(listingId)
                        && testDriveId.equals(s.getBookedByTestDriveId()))
                .findFirst()
                .ifPresent(slot -> {
                    slot.setIsBooked(false);
                    slot.setBookedByTestDriveId(null);
                    slotRepository.save(slot);
                });
    }

    private Long fetchSellerIdFromListing(Long listingId) {
        try {
            CarListingClientResponse response = carListingFeignClient.getListingById(listingId);
            if (response != null && response.getData() != null) {
                CarListingClientResponse.ListingData data = response.getData();
                if (!"ACTIVE".equalsIgnoreCase(data.getStatus())) {
                    throw new InvalidOperationException("Test drives only for ACTIVE listings.");
                }
                if (Boolean.FALSE.equals(data.getTestDriveAvailable())) {
                    throw new InvalidOperationException("Test drive not available for this listing.");
                }
                return data.getSellerId();
            }
        } catch (InvalidOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Could not reach car-listing-service for listingId={}: {}", listingId, ex.getMessage());
        }
        return null;
    }

    private void sendNotification(Long userId, String type, String title, String body) {
        try {
            notificationFeignClient.sendNotification(
                    NotificationClientRequest.builder()
                            .userId(userId)
                            .notificationType(type)
                            .channel("IN_APP")
                            .title(title)
                            .body(body)
                            .build()
            );
        } catch (Exception e) {
            log.error("Notification failed userId={}: {}", userId, e.getMessage());
            // Never fail business logic due to notification failure
        }
    }
}
