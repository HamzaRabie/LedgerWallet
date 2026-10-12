package com.ledgerwallet.userservice.infrastructure.services;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.common.UserErrors;
import com.ledgerwallet.userservice.application.dtos.AuthResponse;
import com.ledgerwallet.userservice.application.dtos.LoginRequest;
import com.ledgerwallet.userservice.application.dtos.RegisterRequest;
import com.ledgerwallet.userservice.application.dtos.UserCreatedEvent;
import com.ledgerwallet.userservice.application.mappers.UserMapper;
import com.ledgerwallet.userservice.application.services.AuthService;
import com.ledgerwallet.userservice.application.services.JwtService;
import com.ledgerwallet.userservice.domain.model.OutboxEvent;
import com.ledgerwallet.userservice.domain.model.OutboxEventStatus;
import com.ledgerwallet.userservice.domain.model.User;
import com.ledgerwallet.userservice.domain.model.UserStatus;
import com.ledgerwallet.userservice.infrastructure.persistence.repository.OutboxEventRepository;
import com.ledgerwallet.userservice.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String USER_CREATED_EVENT_TYPE = "USER_CREATED";

    private final UserRepository userRepo;
    private final OutboxEventRepository outboxEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public AuthServiceImpl(
            UserRepository userRepository,
            OutboxEventRepository outboxEventRepository,
            PasswordEncoder bCryptPasswordEncoder,
            JwtService jwtService,
            ObjectMapper objectMapper
    ) {
        userRepo = userRepository;
        this.outboxEventRepository = outboxEventRepository;
        passwordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Result<AuthResponse> register(RegisterRequest request) {
        var existed = userRepo.existsByUsername(request.username());
        if(existed) return Result.failure(UserErrors.usernameAlreadyExists());

        existed = userRepo.existsByEmail(request.email()) ;
        if(existed) return Result.failure(UserErrors.emailAlreadyExists());

        existed = userRepo.existsByPhone(request.phone()) ;
        if(existed) return Result.failure(UserErrors.phoneAlreadyExists());

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = UserMapper.toUser(request , hashedPassword);
        var savedUser  = userRepo.save(user);
        outboxEventRepository.save(createUserCreatedOutboxEvent(savedUser));

        var token = jwtService.generateToken(savedUser);

        AuthResponse response = UserMapper.toAuthResponse(savedUser , token );

        return Result.success(response);
    }

    @Override
    public Result<AuthResponse> login(LoginRequest request) {
        var user = userRepo.findByEmail(request.emailOrUsername())
                .or(() -> userRepo.findByUsername(request.emailOrUsername()));

        if (user.isEmpty()) {
            return Result.failure(UserErrors.invalidCredentials());
        }

        var existedUser = user.get();

        if (!passwordEncoder.matches(request.password(), existedUser.getPasswordHash())) {
            return Result.failure(UserErrors.invalidCredentials());
        }

        if (existedUser.getStatus() != UserStatus.ACTIVE) {
            return Result.failure(UserErrors.inactiveUser());
        }

        var token = jwtService.generateToken(existedUser);
        var response = UserMapper.toAuthResponse(existedUser, token);

        return Result.success(response);

    }

    private OutboxEvent createUserCreatedOutboxEvent(User user) {
        var eventId = UUID.randomUUID();
        var event = new UserCreatedEvent(
                eventId,
                USER_CREATED_EVENT_TYPE,
                Instant.now(),
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(eventId);
        outboxEvent.setTopic(USER_EVENTS_TOPIC);
        outboxEvent.setEventType(USER_CREATED_EVENT_TYPE);
        outboxEvent.setAggregateId(user.getId());
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setRetryCount(0);

        return outboxEvent;
    }

    private String toJson(UserCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize user created event", ex);
        }
    }
}
