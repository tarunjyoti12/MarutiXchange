package com.marutixchange.user_service.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserResponse user;
}