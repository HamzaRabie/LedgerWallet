package com.ledgerwallet.userservice.infrastructure.cache;

import com.ledgerwallet.userservice.application.dtos.UserResponse;
import com.ledgerwallet.userservice.application.mappers.UserMapper;
import com.ledgerwallet.userservice.domain.model.UserStatus;
import com.ledgerwallet.userservice.infrastructure.persistence.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserCacheService {
    private final UserRepository userRepository;

    public CurrentUserCacheService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(
            cacheNames = "users:me",
            key = "#userId",
            unless = "#result == null"
    )
    public UserResponse findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .map(UserMapper::toResponse)
                .orElse(null);
    }
}
