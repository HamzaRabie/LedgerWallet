package com.ledgerwallet.walletservice.application.dtos;

public record LedgerHashResult(
        String hash,
        long nonce,
        int difficulty
) {
}
