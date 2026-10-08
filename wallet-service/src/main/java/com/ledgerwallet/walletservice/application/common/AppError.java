package com.ledgerwallet.walletservice.application.common;

public record AppError(
        String code,
        String message
) {
}
