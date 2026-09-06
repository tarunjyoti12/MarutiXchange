package com.marutixchange.ar.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArSessionRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    private Long userId;

    private String carName;

    private String deviceType;
}
