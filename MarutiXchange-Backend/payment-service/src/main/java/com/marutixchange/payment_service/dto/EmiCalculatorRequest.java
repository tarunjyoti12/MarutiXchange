package com.marutixchange.payment_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmiCalculatorRequest {

    @NotNull(message = "Loan amount is required")
    @Min(value = 50000,
            message = "Minimum loan amount is 50000")
    private Double loanAmount;

    @NotNull(message = "Interest rate is required")
    private Double annualInterestRate;

    @NotNull(message = "Tenure is required")
    @Min(value = 12,
            message = "Minimum tenure is 12 months")
    private Integer tenureMonths;
}