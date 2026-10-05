package com.ledgerwallet.userservice.application.common;

public class Result<T> {
    private final boolean success;
    private final T data;
    private final AppError error;

    private Result(boolean success, T data, AppError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, null, new AppError(code, message));
    }

    public static <T> Result<T> failure(AppError error) {
        return new Result<>(false, null, error);
    }

    public boolean isSuccess() {return success;}

    public boolean isFailure() {
        return !success;
    }

    public T getDataOrThrow() {
        if (isFailure()) {
            throw new IllegalStateException("Cannot get data from a failed result");
        }

        return data;
    }

    public AppError getErrorOrThrow() {
        if (isSuccess()) {
            throw new IllegalStateException("Cannot get error from a successful result");
        }

        return error;
    }
}
