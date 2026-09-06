package com.marutixchange.user_service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100)
    private String name;

    private String phoneNumber;

    private String profilePictureUrl;
}