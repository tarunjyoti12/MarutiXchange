package com.marutixchange.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "car-listing-service", url = "${app.services.car-listing-url:http://localhost:8068}")
public interface CarListingClient {

    // Called after order is PAID to mark the car listing as SOLD
    @PostMapping("/api/v1/listings/{id}/sold")
    void markAsSold(@PathVariable("id") Long listingId);
}
