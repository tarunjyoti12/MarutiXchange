package com.marutixchange.discovery_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DiscoveryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.marutixchange.discovery_service
                        .config.SecurityConfig.class
        )
)
class DiscoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getStatus_shouldReturn200() throws Exception {
        mockMvc.perform(get("/discovery/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service")
                        .value(
                                "MarutiXchange Discovery Service"))
                .andExpect(jsonPath("$.status")
                        .value("UP"));
    }

    @Test
    @WithMockUser
    void getInfo_shouldReturn200() throws Exception {
        mockMvc.perform(get("/discovery/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform")
                        .value("MarutiXchange"))
                .andExpect(jsonPath("$.service")
                        .value("Discovery Service"));
    }

    @Test
    @WithMockUser
    void getHealth_shouldReturn200() throws Exception {
        mockMvc.perform(get("/discovery/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("UP"))
                .andExpect(jsonPath("$.service")
                        .value("discovery-service"));
    }
}