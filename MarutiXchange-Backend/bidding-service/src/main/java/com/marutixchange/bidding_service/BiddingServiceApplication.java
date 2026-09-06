package com.marutixchange.bidding_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 🔥 IMPORTANT
public class BiddingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiddingServiceApplication.class, args);
	}
}