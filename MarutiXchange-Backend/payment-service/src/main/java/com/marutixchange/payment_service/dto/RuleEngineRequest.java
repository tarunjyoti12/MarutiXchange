package com.marutixchange.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Rule Engine microservice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleEngineRequest {

    private String type; // "payment"
    private Double price;
    private Long userId;
    private Long sellerId;
    private Boolean blacklisted;
    private Long timestamp;
    private String carListingId;
    private String auctionId;
    private String paymentMethod;
}
