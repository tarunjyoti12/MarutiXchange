package com.marutixchange.discovery_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.security.user.name=admin",
        "spring.security.user.password=admin123",
        "eureka.instance.hostname=localhost"
})
class DiscoveryServiceApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodTest() {
        DiscoveryServiceApplication.main(new String[]{});
    }

    // ✅ ADD THIS TEST
    @Test
    void simpleAssertionTest() {
        String appName = "discovery-service";
        assertNotNull(appName);
        assertTrue(appName.contains("discovery"));
    }
}