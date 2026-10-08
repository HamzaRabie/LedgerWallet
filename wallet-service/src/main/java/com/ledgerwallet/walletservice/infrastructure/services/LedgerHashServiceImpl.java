package com.ledgerwallet.walletservice.infrastructure.services;

import com.ledgerwallet.walletservice.application.dtos.LedgerHashResult;
import com.ledgerwallet.walletservice.application.services.LedgerHashService;
import com.ledgerwallet.walletservice.domain.model.WalletTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class LedgerHashServiceImpl implements LedgerHashService {
    private final int difficulty;

    public LedgerHashServiceImpl(
            @Value("${ledger.hash.difficulty:3}") int difficulty
    ) {
        this.difficulty = difficulty;
    }

    @Override
    public LedgerHashResult generateHash(WalletTransaction transaction, String previousHash) {
        String requiredPrefix = "0".repeat(difficulty);
        long nonce = 0;

        while (true) {
            String hashInput = buildHashInput(transaction, previousHash, nonce, difficulty);
            String hash = sha256(hashInput);

            if (hash.startsWith(requiredPrefix)) {
                return new LedgerHashResult(hash, nonce, difficulty);
            }

            nonce++;
        }
    }

    private String buildHashInput(
            WalletTransaction transaction,
            String previousHash,
            long nonce,
            int difficulty
    ) {
        return String.join("|",
                nullToEmpty(transaction.getType()),
                nullToEmpty(transaction.getSourceWalletId()),
                nullToEmpty(transaction.getDestinationWalletId()),
                nullToEmpty(transaction.getAmount()),
                nullToEmpty(transaction.getStatus()),
                nullToEmpty(transaction.getIdempotencyKey()),
                nullToEmpty(previousHash),
                String.valueOf(nonce),
                String.valueOf(difficulty)
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashBytes.length * 2);

            for (byte hashByte : hashBytes) {
                hex.append(String.format("%02x", hashByte));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}
