package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.TestDriveRequest;
import com.marutixchange.bidding_service.entity.TestDrive;
import com.marutixchange.bidding_service.repository.TestDriveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDriveServiceImpl implements TestDriveService {

    private final TestDriveRepository testDriveRepository;

    @Override
    @Transactional
    public TestDrive scheduleTestDrive(TestDriveRequest request, Long buyerId) {

        TestDrive testDrive = TestDrive.builder()
                .carListingId(request.getCarListingId())
                .buyerId(buyerId)
                .sellerId(request.getSellerId())
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .location(request.getLocation())
                .notes(request.getNotes())
                .status(TestDrive.TestDriveStatus.REQUESTED)
                .build();

        return testDriveRepository.save(testDrive);
    }

    @Override
    @Transactional
    public TestDrive confirmTestDrive(Long id) {
        TestDrive td = testDriveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test drive not found"));

        td.setStatus(TestDrive.TestDriveStatus.CONFIRMED);
        return testDriveRepository.save(td);
    }

    @Override
    @Transactional
    public TestDrive cancelTestDrive(Long id, String reason) {
        TestDrive td = testDriveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test drive not found"));

        td.setStatus(TestDrive.TestDriveStatus.CANCELLED);
        td.setCancellationReason(reason);

        return testDriveRepository.save(td);
    }

    @Override
    @Transactional
    public TestDrive completeTestDrive(Long id) {
        TestDrive td = testDriveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test drive not found"));

        td.setStatus(TestDrive.TestDriveStatus.COMPLETED);
        return testDriveRepository.save(td);
    }

    @Override
    public List<TestDrive> getBuyerTestDrives(Long buyerId) {
        return testDriveRepository.findByBuyerId(buyerId);
    }

    @Override
    public List<TestDrive> getSellerTestDrives(Long sellerId) {
        return testDriveRepository.findBySellerId(sellerId);
    }
}