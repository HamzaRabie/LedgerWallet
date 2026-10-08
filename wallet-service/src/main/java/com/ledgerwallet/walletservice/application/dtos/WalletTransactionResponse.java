package com.ledgerwallet.walletservice.application.dtos;

import com.ledgerwallet.walletservice.domain.model.TransactionStatus;
import com.ledgerwallet.walletservice.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletTransactionResponse(
        UUID id,
        TransactionType type,
        UUID sourceWalletId,
        UUID destinationWalletId,
        BigDecimal amount,
        TransactionStatus status,
        String idempotencyKey,
        String previousHash,
        String currentHash,
        Long nonce,
        Integer difficulty,
        Instant createdAt,
        Instant updatedAt
) {
}
