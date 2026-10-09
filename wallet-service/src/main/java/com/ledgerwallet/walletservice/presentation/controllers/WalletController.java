package com.ledgerwallet.walletservice.presentation.controllers;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.dtos.DepositRequest;
import com.ledgerwallet.walletservice.application.dtos.TransferRequest;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;
import com.ledgerwallet.walletservice.application.dtos.WithdrawRequest;
import com.ledgerwallet.walletservice.application.services.DepositService;
import com.ledgerwallet.walletservice.application.services.TransferService;
import com.ledgerwallet.walletservice.application.services.WithdrawService;
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
    private final WithdrawService withdrawService;
    private final TransferService transferService;

    public WalletController(
            DepositService depositService,
            WithdrawService withdrawService,
            TransferService transferService
    ) {
        this.depositService = depositService;
        this.withdrawService = withdrawService;
        this.transferService = transferService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> deposit(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody DepositRequest request
    ) {
        Result<WalletTransactionResponse> result = depositService.deposit(userId, request);

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
                        "Deposit completed successfully",
                        result.getDataOrThrow(),
                        null
                )
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> withdraw(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody WithdrawRequest request
    ) {
        Result<WalletTransactionResponse> result = withdrawService.withdraw(userId, request);

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
                        "Withdrawal completed successfully",
                        result.getDataOrThrow(),
                        null
                )
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> transfer(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody TransferRequest request
    ) {
        Result<WalletTransactionResponse> result = transferService.transfer(userId, request);

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
                        "Transfer completed successfully",
                        result.getDataOrThrow(),
                        null
                )
        );
    }
}
