package com.ledgerwallet.userservice.infrastructure.services;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.common.UserErrors;
import com.ledgerwallet.userservice.application.dtos.UserResponse;
import com.ledgerwallet.userservice.application.mappers.UserMapper;
import com.ledgerwallet.userservice.application.services.UserService;
import com.ledgerwallet.userservice.domain.model.User;
import com.ledgerwallet.userservice.domain.model.UserStatus;
import com.ledgerwallet.userservice.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Result<UserResponse> getCurrentUser(Authentication authentication) {
        try {
            UUID userId = UUID.fromString(authentication.getName());
            return findById(userId);
        } catch (IllegalArgumentException ex) {
            return Result.failure(UserErrors.invalidCredentials());
        }
    }

    @Override
    public Result<List<UserResponse>> getAllUsers() {
        var users = userRepository.findAll()
                .stream()
                .filter(user -> user.getStatus() != UserStatus.DELETED)
                .map(UserMapper::toResponse)
                .toList();

        return Result.success(users);
    }

    @Override
    public Result<UserResponse> findById(UUID userId) {
        return toUserResponseResult(userRepository.findById(userId));
    }

    @Override
    public Result<UserResponse> findByEmail(String email) {
        return toUserResponseResult(userRepository.findByEmail(email));
    }

    @Override
    public Result<UserResponse> findByUsername(String username) {
        return toUserResponseResult(userRepository.findByUsername(username));
    }

    private Result<UserResponse> toUserResponseResult(Optional<User> user) {
        if (user.isEmpty()) return Result.failure(UserErrors.userNotFound());

        var existingUser = user.get();

        if (existingUser.getStatus() == UserStatus.DELETED) {
            return Result.failure(UserErrors.userNotFound());
        }

        if (existingUser.getStatus() != UserStatus.ACTIVE) {
            return Result.failure(UserErrors.inactiveUser());
        }

        var userResponse = UserMapper.toResponse(existingUser);
        return Result.success(userResponse);
    }
}
