package com.ledgerwallet.walletservice.presentation.response;

import com.ledgerwallet.walletservice.application.common.AppError;

public record ApiResponse<T>(
        boolean success,
        int statusCode,
        String message,
        T data,
        AppError error
) {
}
