package com.marutixchange.test_drive_service.service;

import com.marutixchange.test_drive_service.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TestDriveService {

    /** Buyer requests a test drive booking */
    TestDriveResponse bookTestDrive(TestDriveRequest request);

    /** Get test drive by ID */
    TestDriveResponse getById(Long id);

    /** Seller confirms a pending test drive */
    TestDriveResponse confirmTestDrive(Long id);

    /** Seller or buyer cancels a test drive */
    TestDriveResponse cancelTestDrive(Long id, CancelRequest cancelRequest);

    /** Seller marks a test drive as completed */
    TestDriveResponse completeTestDrive(Long id);

    /** Seller marks buyer as no-show */
    TestDriveResponse markNoShow(Long id);

    /** Buyer submits rating and feedback after completion */
    TestDriveResponse submitFeedback(Long id, FeedbackRequest feedbackRequest);

    /** Seller adds internal notes */
    TestDriveResponse addSellerNotes(Long id, String notes);

    /** Paginated list for a buyer */
    Page<TestDriveResponse> getByBuyer(Long buyerId, int page, int size);

    /** Paginated list for a seller */
    Page<TestDriveResponse> getBySeller(Long sellerId, int page, int size);

    /** All test drives for a specific listing */
    List<TestDriveResponse> getByListing(Long listingId);
}
