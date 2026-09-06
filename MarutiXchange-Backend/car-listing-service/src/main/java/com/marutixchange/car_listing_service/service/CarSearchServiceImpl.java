package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.dto.*;
import com.marutixchange.car_listing_service.entity.CarListing;
import com.marutixchange.car_listing_service.repository.CarListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarSearchServiceImpl implements CarSearchService {

    private final CarListingRepository repository;

    @Override
    public Page<CarListingResponse> searchListings(CarSearchRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("createdAt").descending());

        return repository.searchListings(
                        request.getBrand(),
                        request.getCity(),
                        request.getPriceFrom(),
                        request.getPriceTo(),
                        pageable)
                .map(CarListingResponse::fromEntity);
    }

    @Override
    public List<CarListingResponse> getRecommendations(
            String brand, String city) {

        return repository.searchListings(
                        brand, city, null, null,
                        PageRequest.of(0, 5))
                .stream()
                .map(CarListingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarListingResponse> compareListings(
            List<Long> listingIds) {

        return repository.findAllById(listingIds)
                .stream()
                .map(CarListingResponse::fromEntity)
                .collect(Collectors.toList());
    }
}