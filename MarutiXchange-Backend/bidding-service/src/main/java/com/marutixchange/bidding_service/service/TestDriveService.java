package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.TestDriveRequest;
import com.marutixchange.bidding_service.entity.TestDrive;

import java.util.List;

public interface TestDriveService {

    TestDrive scheduleTestDrive(TestDriveRequest request, Long buyerId);

    TestDrive confirmTestDrive(Long id);

    TestDrive cancelTestDrive(Long id, String reason);

    TestDrive completeTestDrive(Long id);

    List<TestDrive> getBuyerTestDrives(Long buyerId);

    List<TestDrive> getSellerTestDrives(Long sellerId);
}