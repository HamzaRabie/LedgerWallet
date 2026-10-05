package com.ledgerwallet.userservice.application.services;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.dtos.AuthResponse;
import com.ledgerwallet.userservice.application.dtos.LoginRequest;
import com.ledgerwallet.userservice.application.dtos.RegisterRequest;

public interface AuthService {
    Result<AuthResponse> register(RegisterRequest request);
    Result<AuthResponse> login(LoginRequest request);
}
