package com.marutixchange.bidding_service.controller;

import com.marutixchange.bidding_service.dto.ApiResponse;
import com.marutixchange.bidding_service.dto.TestDriveRequest;
import com.marutixchange.bidding_service.service.TestDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test-drives")
@RequiredArgsConstructor
public class TestDriveController {

    private final TestDriveService testDriveService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> scheduleTestDrive(
            @Valid @RequestBody TestDriveRequest request,
            @RequestAttribute("userId") Long buyerId) {

        Object response = testDriveService.scheduleTestDrive(request, buyerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Test drive scheduled", response));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<?>> confirmTestDrive(@PathVariable Long id) {
        Object response = testDriveService.confirmTestDrive(id);
        return ResponseEntity.ok(ApiResponse.success("Test drive confirmed", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelTestDrive(
            @PathVariable Long id,
            @RequestParam String reason) {

        Object response = testDriveService.cancelTestDrive(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Test drive cancelled", response));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<?>> completeTestDrive(@PathVariable Long id) {
        Object response = testDriveService.completeTestDrive(id);
        return ResponseEntity.ok(ApiResponse.success("Test drive completed", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyTestDrives(
            @RequestAttribute("userId") Long userId) {

        Object response = testDriveService.getBuyerTestDrives(userId);
        return ResponseEntity.ok(ApiResponse.success("Test drives fetched", response));
    }
}