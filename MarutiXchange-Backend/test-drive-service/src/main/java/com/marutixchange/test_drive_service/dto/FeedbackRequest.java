package com.marutixchange.test_drive_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer buyerRating;

    @Size(max = 1000, message = "Feedback too long")
    private String buyerFeedback;
}
