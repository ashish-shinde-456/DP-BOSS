package com.ashish.resultswebsite.admin;

import java.time.Instant;

public record AdminUserResponse(
        String phoneNumber,
        String displayName,
        String role,
        boolean active,
        Instant createdAt,
        Instant passwordExpiresAt,
        boolean mustChangePassword
) {

    public static AdminUserResponse from(AdminUser user) {
        Instant now = Instant.now();
        return new AdminUserResponse(
                user.getPhoneNumber(),
                user.getDisplayName(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getPasswordExpiresAt(),
                user.isPasswordExpired(now)
        );
    }
}
