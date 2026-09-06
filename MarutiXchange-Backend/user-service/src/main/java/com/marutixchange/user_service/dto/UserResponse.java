package com.marutixchange.user_service.dto;

import com.marutixchange.user_service.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String role;
    private Boolean active;
    private Boolean isVerified;
    private Boolean accountLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .active(user.getActive())
                .isVerified(user.getIsVerified())
                .accountLocked(user.getAccountLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}