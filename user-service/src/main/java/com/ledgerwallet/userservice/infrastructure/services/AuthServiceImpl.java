package com.ledgerwallet.userservice.infrastructure.services;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.common.UserErrors;
import com.ledgerwallet.userservice.application.dtos.AuthResponse;
import com.ledgerwallet.userservice.application.dtos.LoginRequest;
import com.ledgerwallet.userservice.application.dtos.RegisterRequest;
import com.ledgerwallet.userservice.application.mappers.UserMapper;
import com.ledgerwallet.userservice.application.services.AuthService;
import com.ledgerwallet.userservice.application.services.JwtService;
import com.ledgerwallet.userservice.domain.model.User;
import com.ledgerwallet.userservice.domain.model.UserStatus;
import com.ledgerwallet.userservice.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthServiceImpl(UserRepository userRepository , PasswordEncoder bCryptPasswordEncoder , JwtService  jwtService) {
        userRepo = userRepository;
        passwordEncoder = bCryptPasswordEncoder;
        this.jwtService = jwtService;
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
}
