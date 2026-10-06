package com.ledgerwallet.userservice.application.dtos;

import com.ledgerwallet.userservice.domain.model.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String username,
        String role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
