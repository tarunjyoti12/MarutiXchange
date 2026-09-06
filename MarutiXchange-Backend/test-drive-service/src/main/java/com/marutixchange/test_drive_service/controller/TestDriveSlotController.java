package com.marutixchange.test_drive_service.controller;

import com.marutixchange.test_drive_service.dto.ApiResponse;
import com.marutixchange.test_drive_service.dto.SlotCreateRequest;
import com.marutixchange.test_drive_service.dto.SlotResponse;
import com.marutixchange.test_drive_service.service.TestDriveSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/slots")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Drive Slots", description = "APIs for managing available test drive slots")
public class TestDriveSlotController {

    private final TestDriveSlotService slotService;

    @PostMapping
    @Operation(summary = "Create one or more time slots for a listing (Seller)")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> createSlots(
            @Valid @RequestBody SlotCreateRequest request) {

        List<SlotResponse> slots = slotService.createSlots(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Slots created", slots));
    }

    @GetMapping("/listing/{listingId}")
    @Operation(summary = "Get all future available slots for a listing")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getAllAvailable(
            @PathVariable Long listingId) {

        List<SlotResponse> slots = slotService.getAllAvailableSlots(listingId);
        return ResponseEntity.ok(ApiResponse.success("Available slots fetched", slots));
    }

    @GetMapping("/listing/{listingId}/date")
    @Operation(summary = "Get available slots for a listing on a specific date")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getByDate(
            @PathVariable Long listingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<SlotResponse> slots = slotService.getAvailableSlotsByDate(listingId, date);
        return ResponseEntity.ok(ApiResponse.success("Slots for date fetched", slots));
    }

    @DeleteMapping("/{slotId}")
    @Operation(summary = "Deactivate a slot (Seller removes an unbooked slot)")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long slotId) {
        slotService.deactivateSlot(slotId);
        return ResponseEntity.ok(ApiResponse.success("Slot deactivated", null));
    }
}
