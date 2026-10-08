package com.ledgerwallet.walletservice.application.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal balance,
        String currency,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
