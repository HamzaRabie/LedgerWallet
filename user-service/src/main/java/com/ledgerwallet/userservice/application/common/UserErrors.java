package com.ledgerwallet.userservice.application.common;

public final class UserErrors {
    private UserErrors() {
    }

    public static AppError emailAlreadyExists() {
        return new AppError("EMAIL_ALREADY_EXISTS", "Email is already registered");
    }

    public static AppError usernameAlreadyExists() {
        return new AppError("USERNAME_ALREADY_EXISTS", "Username is already taken");
    }

    public static AppError phoneAlreadyExists() {
        return new AppError("PHONE_ALREADY_EXISTS", "Phone number is already registered");
    }

    public static AppError invalidCredentials() {
        return new AppError("INVALID_CREDENTIALS", "Email, username, or password is incorrect");
    }

    public static AppError userNotFound() {
        return new AppError("USER_NOT_FOUND", "User was not found");
    }

    public static AppError inactiveUser() {
        return new AppError("INACTIVE_USER", "User account is not active");
    }
}
