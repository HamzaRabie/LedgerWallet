package com.ledgerwallet.userservice.application.services;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.dtos.UserResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface UserService {
    Result<UserResponse> getCurrentUser(Authentication authentication);
    Result<List<UserResponse>> getAllUsers();
    Result<UserResponse> findById(UUID userId);
    Result<UserResponse> findByEmail(String email);
    Result<UserResponse> findByUsername(String username);
}
