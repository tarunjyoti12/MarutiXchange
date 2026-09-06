package com.marutixchange.test_drive_service.service.impl;

import com.marutixchange.test_drive_service.dto.SlotCreateRequest;
import com.marutixchange.test_drive_service.dto.SlotResponse;
import com.marutixchange.test_drive_service.entity.TestDriveSlot;
import com.marutixchange.test_drive_service.exception.TestDriveNotFoundException;
import com.marutixchange.test_drive_service.repository.TestDriveSlotRepository;
import com.marutixchange.test_drive_service.service.TestDriveSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDriveSlotServiceImpl implements TestDriveSlotService {

    private final TestDriveSlotRepository slotRepository;

    @Override
    @Transactional
    public List<SlotResponse> createSlots(SlotCreateRequest request) {
        log.info("Creating {} slot(s) for listingId={} on {}",
                request.getSlotTimes().size(), request.getListingId(), request.getSlotDate());

        List<SlotResponse> created = new ArrayList<>();

        for (LocalTime time : request.getSlotTimes()) {

            // Skip if slot already exists for this listing/date/time
            boolean exists = slotRepository
                    .findByListingIdAndSlotDateAndSlotTime(
                            request.getListingId(), request.getSlotDate(), time)
                    .isPresent();

            if (exists) {
                log.warn("Slot already exists for listingId={} at {} {}. Skipping.",
                        request.getListingId(), request.getSlotDate(), time);
                continue;
            }

            TestDriveSlot slot = TestDriveSlot.builder()
                    .listingId(request.getListingId())
                    .sellerId(request.getSellerId())
                    .slotDate(request.getSlotDate())
                    .slotTime(time)
                    .isBooked(false)
                    .isActive(true)
                    .build();

            created.add(SlotResponse.fromEntity(slotRepository.save(slot)));
        }

        log.info("Created {} new slot(s)", created.size());
        return created;
    }

    @Override
    public List<SlotResponse> getAvailableSlotsByDate(Long listingId, LocalDate date) {
        return slotRepository
                .findByListingIdAndSlotDateAndIsBookedFalseAndIsActiveTrue(listingId, date)
                .stream()
                .map(SlotResponse::fromEntity)
                .toList();
    }

    @Override
    public List<SlotResponse> getAllAvailableSlots(Long listingId) {
        return slotRepository
                .findAvailableSlotsFromDate(listingId, LocalDate.now())
                .stream()
                .map(SlotResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateSlot(Long slotId) {
        TestDriveSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new TestDriveNotFoundException(slotId));

        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new com.marutixchange.test_drive_service.exception
                    .InvalidOperationException(
                    "Cannot deactivate a slot that is already booked.");
        }

        slot.setIsActive(false);
        slotRepository.save(slot);
        log.info("Slot id={} deactivated.", slotId);
    }
}
