package com.marutixchange.car_listing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;  // ✅ ADDED IMPORT

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CarListingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarListingServiceApplication.class, args);
	}
}