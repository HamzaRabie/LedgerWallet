package com.ledgerwallet.userservice.application.common;

public record AppError(
        String code,
        String message
) {
}
