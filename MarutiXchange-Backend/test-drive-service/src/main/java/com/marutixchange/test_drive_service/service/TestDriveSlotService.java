package com.marutixchange.test_drive_service.service;

import com.marutixchange.test_drive_service.dto.SlotCreateRequest;
import com.marutixchange.test_drive_service.dto.SlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface TestDriveSlotService {

    /** Seller creates one or more time slots for a listing on a date */
    List<SlotResponse> createSlots(SlotCreateRequest request);

    /** Available slots for a listing on a specific date */
    List<SlotResponse> getAvailableSlotsByDate(Long listingId, LocalDate date);

    /** All future available slots for a listing */
    List<SlotResponse> getAllAvailableSlots(Long listingId);

    /** Deactivate a slot (seller removes it) */
    void deactivateSlot(Long slotId);
}
