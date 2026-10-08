package com.ledgerwallet.walletservice.application.common;

public final class WalletErrors {
    private WalletErrors() {
    }

    public static AppError invalidCredentials() {
        return new AppError("INVALID_CREDENTIALS", "Authentication token is invalid");
    }

    public static AppError walletNotFound() {
        return new AppError("WALLET_NOT_FOUND", "Wallet was not found");
    }

    public static AppError walletAlreadyExists() {
        return new AppError("WALLET_ALREADY_EXISTS", "Wallet already exists for this user");
    }

    public static AppError insufficientBalance() {
        return new AppError("INSUFFICIENT_BALANCE", "Wallet balance is not enough for this operation");
    }

    public static AppError duplicateIdempotencyKey() {
        return new AppError("DUPLICATE_IDEMPOTENCY_KEY", "This payment request was already processed");
    }

    public static AppError invalidAmount() {
        return new AppError("INVALID_AMOUNT", "Amount must be greater than zero");
    }

    public static AppError concurrentWalletUpdate() {
        return new AppError("CONCURRENT_WALLET_UPDATE", "Wallet was updated by another request, please retry");
    }
}
