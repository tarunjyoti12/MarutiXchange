package com.marutixchange.rc_transfer_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RcTransferRequest {
    @NotNull private Long orderId;
    @NotNull private Long carListingId;
    @NotNull private Long buyerId;
    @NotNull private Long sellerId;
    @NotNull private String registrationNumber;
    private String vehicleClass;
    private String rtoOffice;
}
