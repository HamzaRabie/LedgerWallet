package com.ledgerwallet.userservice.presentation.controllers;

import com.ledgerwallet.userservice.application.common.Result;
import com.ledgerwallet.userservice.application.dtos.AuthResponse;
import com.ledgerwallet.userservice.application.dtos.LoginRequest;
import com.ledgerwallet.userservice.application.dtos.RegisterRequest;
import com.ledgerwallet.userservice.application.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Result<AuthResponse> result = authService.register(request);

        if (result.isFailure()) {
            return ResponseEntity.badRequest().body(result.getErrorOrThrow());
        }

        return ResponseEntity.status(201).body(result.getDataOrThrow());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Result<AuthResponse> result = authService.login(request);

        if (result.isFailure()) {
            return ResponseEntity.badRequest().body(result.getErrorOrThrow());
        }

        return ResponseEntity.ok(result.getDataOrThrow());
    }
}
