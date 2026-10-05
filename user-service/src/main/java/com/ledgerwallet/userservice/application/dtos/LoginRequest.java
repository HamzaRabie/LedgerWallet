package com.ledgerwallet.userservice.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email or username is required")
        @Size(max = 255, message = "Email or username must not exceed 255 characters")
        String emailOrUsername,

        @NotBlank(message = "Password is required")
        @Size(max = 100, message = "Password must not exceed 100 characters")
        String password
) {
}
