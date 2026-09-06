package com.marutixchange.test_drive_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelRequest {

    /** Who is cancelling: BUYER | SELLER | SYSTEM */
    @NotBlank(message = "cancelledBy is required")
    private String cancelledBy;

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 500, message = "Reason too long")
    private String cancellationReason;
}
