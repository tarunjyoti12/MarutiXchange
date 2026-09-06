package com.marutixchange.payment_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmiCalculatorResponse {

    private Double loanAmount;
    private Double annualInterestRate;
    private Integer tenureMonths;
    private Double monthlyEmi;

    private Double totalInterest;

    // ✅ KEEP THIS NAME (matches your DTO)
    private Double totalPayable;

    private Double processingFee;
}