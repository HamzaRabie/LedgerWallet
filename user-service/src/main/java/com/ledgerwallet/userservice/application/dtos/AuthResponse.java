package com.ledgerwallet.userservice.application.dtos;

public record AuthResponse(
        String accessToken,
        UserResponse user
) {
}
