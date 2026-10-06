package com.ledgerwallet.userservice.presentation.controllers;

import com.ledgerwallet.userservice.application.dtos.UserResponse;
import com.ledgerwallet.userservice.application.services.UserService;
import com.ledgerwallet.userservice.presentation.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        var result = userService.getCurrentUser(authentication);
        if (result.isFailure()) {
            var error = result.getErrorOrThrow();
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            false,
                            HttpStatus.BAD_REQUEST.value(),
                            error.message(),
                            null,
                            error
                    )
            );
        }

        UserResponse response = result.getDataOrThrow();
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        HttpStatus.OK.value(),
                        "Current user retrieved successfully",
                        response,
                        null
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        var result = userService.getAllUsers();

        if (result.isFailure()) {
            var error = result.getErrorOrThrow();
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(
                            false,
                            HttpStatus.BAD_REQUEST.value(),
                            error.message(),
                            null,
                            error
                    )
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        HttpStatus.OK.value(),
                        "Users retrieved successfully",
                        result.getDataOrThrow(),
                        null
                )
        );
    }
}
