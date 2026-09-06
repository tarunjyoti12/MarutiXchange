package com.marutixchange.rules_engine_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for Rules Engine Service
 *
 * Features:
 * - Drools/KIE rule engine integration
 * - REST API for rule evaluation
 * - Eureka service discovery
 * - Spring Boot 3.2.5
 * - Java 21
 */
@SpringBootApplication
@EnableDiscoveryClient
public class RulesEngineServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RulesEngineServiceApplication.class, args);
	}
}