package com.ledgerwallet.userservice.presentation.response;

import com.ledgerwallet.userservice.application.common.AppError;

public record ApiResponse<T>(
        boolean success,
        int statusCode,
        String message,
        T data,
        AppError error
) {
}