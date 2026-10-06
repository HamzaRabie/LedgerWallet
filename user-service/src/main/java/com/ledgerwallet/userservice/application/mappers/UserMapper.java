package com.ledgerwallet.userservice.application.mappers;

import com.ledgerwallet.userservice.application.dtos.AuthResponse;
import com.ledgerwallet.userservice.application.dtos.RegisterRequest;
import com.ledgerwallet.userservice.application.dtos.UserResponse;
import com.ledgerwallet.userservice.domain.model.UserRole;
import com.ledgerwallet.userservice.domain.model.User;
import com.ledgerwallet.userservice.domain.model.UserStatus;

public final class UserMapper {
    private UserMapper() {
    }

    public static User toUser(RegisterRequest request, String passwordHash) {
        User user = new User();

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);

        return user;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static AuthResponse toAuthResponse(User user, String accessToken) {
        return new AuthResponse(
                accessToken,
                toResponse(user)
        );
    }
}
