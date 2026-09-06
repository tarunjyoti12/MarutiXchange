package com.marutixchange.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_role", columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= BASIC =================

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // ================= ROLE =================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.BUYER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ================= OTP =================

    @Column(name = "otp", length = 10)
    private String otp;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    // ================= SECURITY =================

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    // ================= PASSWORD RESET =================

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    // ================= AUDIT =================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= ENUM =================

    public enum Role {
        BUYER,
        SELLER,
        DEALER,
        ADMIN
    }
}