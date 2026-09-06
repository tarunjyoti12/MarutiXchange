package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.client.RuleEngineClient;
import com.marutixchange.car_listing_service.client.NotificationClient; // ✅ ADDED
import com.marutixchange.car_listing_service.dto.NotificationRequest;  // ✅ ADDED
import com.marutixchange.car_listing_service.dto.CarListingRequest;
import com.marutixchange.car_listing_service.dto.CarListingResponse;
import com.marutixchange.car_listing_service.dto.RuleContext;
import com.marutixchange.car_listing_service.entity.CarImage;
import com.marutixchange.car_listing_service.entity.CarListing;
import com.marutixchange.car_listing_service.exception.CarListingNotFoundException;
import com.marutixchange.car_listing_service.exception.InvalidListingException;
import com.marutixchange.car_listing_service.repository.CarImageRepository;
import com.marutixchange.car_listing_service.repository.CarListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarListingServiceImpl implements CarListingService {

    private final CarListingRepository listingRepository;
    private final CarImageRepository imageRepository;
    private final RuleEngineClient ruleEngineClient;
    private final NotificationClient notificationClient; // ✅ ADDED

    @Value("${app.upload.dir:uploads/cars}")
    private String uploadDir;

    @Override
    @Transactional
    public CarListingResponse createListing(CarListingRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        RuleContext context = new RuleContext();

        context.setType("CAR_LISTING");
        context.setPrice(request.getAskingPrice());
        context.setUserId(request.getSellerId());

        context.setBrand(request.getBrand() != null
                ? request.getBrand().trim().toLowerCase()
                : null);

        context.setFuelType(request.getFuelType() != null
                ? request.getFuelType().name()
                : null);

        context.setTransmission(request.getTransmission() != null
                ? request.getTransmission().name()
                : null);

        context.setYear(request.getYear());

        context.setDuplicate(false);
        context.setSellerVerified(true);
        context.setBlacklisted(false);
        context.setRisky(false);
        context.setRuleMatched(false);

        log.info("🚀 Calling Rule Engine with: {}", context);

        RuleContext result = null;

        try {
            result = ruleEngineClient.evaluateRules(context);
        } catch (Exception e) {
            log.error("❌ Rule Engine Error: {}", e.getMessage());
        }

        log.info("✅ Rule Engine Response: {}", result);

        if (result == null) {
            log.warn("⚠️ Rule Engine not responding → applying fallback");
            result = context;
            result.setApproved(true);
            result.setMessage("Fallback applied - Rule engine unavailable");
        }

        if (!result.isApproved()) {
            throw new InvalidListingException(result.getMessage());
        }

        CarListing listing = CarListing.builder()
                .sellerId(request.getSellerId())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .variant(request.getVariant())
                .fuelType(request.getFuelType())
                .transmission(request.getTransmission())
                .color(request.getColor())
                .mileage(request.getMileage())
                .engineCc(request.getEngineCc())
                .ownerNumber(request.getOwnerNumber())
                .registrationNumber(request.getRegistrationNumber())
                .vinNumber(request.getVinNumber())
                .rcNumber(request.getRcNumber())
                .askingPrice(request.getAskingPrice())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .description(request.getDescription())
                .testDriveAvailable(request.getTestDriveAvailable() != null
                        ? request.getTestDriveAvailable()
                        : true)
                .insuranceValidTill(request.getInsuranceValidTill())
                .status(CarListing.ListingStatus.ACTIVE)
                .build();

        CarListing savedListing = listingRepository.save(listing);

        // 🔔 NOTIFICATION (ONLY ADDITION)
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(savedListing.getSellerId());
            notification.setChannel("IN_APP");
            notification.setNotificationType("CAR_LISTING_APPROVED");
            notification.setTitle("Car Listed");
            notification.setBody("Your car has been listed successfully!");

            notificationClient.sendNotification(notification);

            log.info("✅ Notification sent successfully");
        } catch (Exception e) {
            log.error("❌ Notification failed: {}", e.getMessage());
        }

        return CarListingResponse.fromEntity(savedListing);
    }

    // REST OF YOUR FILE UNCHANGED ↓↓↓

    @Override
    public CarListingResponse getListingById(Long id) {
        CarListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new CarListingNotFoundException(id));
        incrementViewCount(id);
        return CarListingResponse.fromEntity(listing);
    }

    @Override
    @Transactional
    public CarListingResponse updateListing(Long id, CarListingRequest request) {
        CarListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new CarListingNotFoundException(id));

        if (request.getBrand() != null) listing.setBrand(request.getBrand());
        if (request.getModel() != null) listing.setModel(request.getModel());
        if (request.getYear() != null) listing.setYear(request.getYear());
        if (request.getAskingPrice() != null) listing.setAskingPrice(request.getAskingPrice());
        if (request.getCity() != null) listing.setCity(request.getCity());

        return CarListingResponse.fromEntity(
                listingRepository.save(listing));
    }

    @Override
    @Transactional
    public void deleteListing(Long id) {
        CarListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new CarListingNotFoundException(id));
        listing.setStatus(CarListing.ListingStatus.EXPIRED);
        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public void markAsSold(Long id) {
        CarListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new CarListingNotFoundException(id));
        listing.setStatus(CarListing.ListingStatus.SOLD);
        listing.setSoldAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public CarListingResponse uploadImages(Long id, List<MultipartFile> images) throws IOException {
        CarListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new CarListingNotFoundException(id));

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            imageRepository.save(
                    CarImage.builder()
                            .carListing(listing)
                            .imageUrl(uploadDir + "/" + fileName)
                            .isPrimary(i == 0)
                            .build()
            );
        }

        return CarListingResponse.fromEntity(
                listingRepository.findById(id).get());
    }

    @Override
    public Page<CarListingResponse> getAllListings(int page, int size, String sortBy) {
        return listingRepository.findByStatus(
                        CarListing.ListingStatus.ACTIVE,
                        PageRequest.of(page, size, Sort.by(sortBy).descending()))
                .map(CarListingResponse::fromEntity);
    }

    @Override
    public List<CarListingResponse> getFeaturedListings() {
        return listingRepository.findFeaturedListings()
                .stream()
                .map(CarListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarListingResponse> getSellerListings(Long sellerId) {
        return listingRepository.findBySellerId(sellerId)
                .stream()
                .map(CarListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void boostListing(Long id) {
        CarListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new CarListingNotFoundException(id));
        listing.setIsBoosted(true);
        listing.setStatus(CarListing.ListingStatus.BOOSTED);
        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long id) {
        listingRepository.findById(id).ifPresent(listing -> {
            listing.setViewCount(
                    listing.getViewCount() != null
                            ? listing.getViewCount() + 1
                            : 1);
            listingRepository.save(listing);
        });
    }
}