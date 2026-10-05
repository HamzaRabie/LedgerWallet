package com.ledgerwallet.userservice.application.services;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.dtos.UserResponse;

import java.util.UUID;

public interface UserService {
    Result<UserResponse> findById(UUID userId);
    Result<UserResponse> findByEmail(String email);
    Result<UserResponse> findByUsername(String username);
}
