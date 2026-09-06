package com.marutixchange.test_drive_service.client;

import com.marutixchange.test_drive_service.dto.CarListingClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "car-listing-service",
        url = "${app.services.car-listing-url}"
)
public interface CarListingFeignClient {

    @GetMapping("/api/v1/listings/{id}")
    CarListingClientResponse getListingById(@PathVariable("id") Long id);
}
