package com.ledgerwallet.walletservice.presentation.controllers;

import com.ledgerwallet.walletservice.application.common.AppError;
import com.ledgerwallet.walletservice.application.dtos.DepositRequest;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;
import com.ledgerwallet.walletservice.application.services.DepositService;
import com.ledgerwallet.walletservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    private final DepositService depositService;

    public WalletController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> deposit(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody DepositRequest request
    ) {
        var result = depositService.deposit(userId, request);

        if (result.isFailure()) {
            var error = result.getErrorOrThrow();
            var status = mapErrorStatus(error);

            return ResponseEntity.status(status).body(
                    new ApiResponse<>(
                            false,
                            status.value(),
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
                        "Deposit completed successfully",
                        result.getDataOrThrow(),
                        null
                )
        );
    }

    private HttpStatus mapErrorStatus(AppError error) {
        return switch (error.code()) {
            case "WALLET_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONCURRENT_WALLET_UPDATE", "DUPLICATE_IDEMPOTENCY_KEY" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
