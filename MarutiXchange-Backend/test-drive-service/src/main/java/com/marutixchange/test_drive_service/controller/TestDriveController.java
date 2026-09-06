package com.marutixchange.test_drive_service.controller;

import com.marutixchange.test_drive_service.dto.*;
import com.marutixchange.test_drive_service.service.TestDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test-drives")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Drive", description = "APIs for test drive booking and management")
public class TestDriveController {

    private final TestDriveService testDriveService;

    // ─── Book ─────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Book a test drive (Buyer)")
    public ResponseEntity<ApiResponse<TestDriveResponse>> bookTestDrive(
            @Valid @RequestBody TestDriveRequest request) {

        TestDriveResponse response = testDriveService.bookTestDrive(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Test drive booked successfully", response));
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get test drive by ID")
    public ResponseEntity<ApiResponse<TestDriveResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Test drive fetched", testDriveService.getById(id)));
    }

    @GetMapping("/buyer/{buyerId}")
    @Operation(summary = "Get all test drives for a buyer (paginated)")
    public ResponseEntity<ApiResponse<Page<TestDriveResponse>>> getByBuyer(
            @PathVariable Long buyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TestDriveResponse> result = testDriveService.getByBuyer(buyerId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Buyer test drives fetched", result));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get all test drives for a seller (paginated)")
    public ResponseEntity<ApiResponse<Page<TestDriveResponse>>> getBySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TestDriveResponse> result = testDriveService.getBySeller(sellerId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Seller test drives fetched", result));
    }

    @GetMapping("/listing/{listingId}")
    @Operation(summary = "Get all test drives for a listing")
    public ResponseEntity<ApiResponse<List<TestDriveResponse>>> getByListing(
            @PathVariable Long listingId) {

        List<TestDriveResponse> result = testDriveService.getByListing(listingId);
        return ResponseEntity.ok(ApiResponse.success("Listing test drives fetched", result));
    }

    // ─── Status Transitions ───────────────────────────────────────────────────

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirm a pending test drive (Seller)")
    public ResponseEntity<ApiResponse<TestDriveResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Test drive confirmed", testDriveService.confirmTestDrive(id)));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a test drive (Buyer or Seller)")
    public ResponseEntity<ApiResponse<TestDriveResponse>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelRequest cancelRequest) {

        return ResponseEntity.ok(
                ApiResponse.success("Test drive cancelled",
                        testDriveService.cancelTestDrive(id, cancelRequest)));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark test drive as completed (Seller)")
    public ResponseEntity<ApiResponse<TestDriveResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Test drive completed",
                        testDriveService.completeTestDrive(id)));
    }

    @PatchMapping("/{id}/no-show")
    @Operation(summary = "Mark buyer as no-show (Seller)")
    public ResponseEntity<ApiResponse<TestDriveResponse>> noShow(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Marked as no-show",
                        testDriveService.markNoShow(id)));
    }

    // ─── Feedback & Notes ────────────────────────────────────────────────────

    @PatchMapping("/{id}/feedback")
    @Operation(summary = "Submit rating and feedback (Buyer, after completion)")
    public ResponseEntity<ApiResponse<TestDriveResponse>> feedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest feedbackRequest) {

        return ResponseEntity.ok(
                ApiResponse.success("Feedback submitted",
                        testDriveService.submitFeedback(id, feedbackRequest)));
    }

    @PatchMapping("/{id}/seller-notes")
    @Operation(summary = "Add seller notes to a test drive")
    public ResponseEntity<ApiResponse<TestDriveResponse>> sellerNotes(
            @PathVariable Long id,
            @RequestParam String notes) {

        return ResponseEntity.ok(
                ApiResponse.success("Notes saved",
                        testDriveService.addSellerNotes(id, notes)));
    }
}
