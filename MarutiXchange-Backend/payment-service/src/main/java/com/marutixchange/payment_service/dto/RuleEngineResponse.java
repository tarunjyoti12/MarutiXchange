package com.marutixchange.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO from Rule Engine microservice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleEngineResponse {

    private Boolean approved;
    private String message;
    private Map<String, Object> violations;
    private Map<String, Object> warnings;
    private Map<String, Object> suggestions;
    private Long evaluationTime;
}
