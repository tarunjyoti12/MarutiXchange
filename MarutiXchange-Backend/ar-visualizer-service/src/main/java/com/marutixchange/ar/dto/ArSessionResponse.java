package com.marutixchange.ar.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArSessionResponse {
    private String sessionId;
    private Long carId;
    private String carName;
    private String carImageUrl;
    private String status;
    private String message;
}
