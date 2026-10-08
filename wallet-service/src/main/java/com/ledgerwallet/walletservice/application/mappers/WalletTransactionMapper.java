package com.ledgerwallet.walletservice.application.mappers;

import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;
import com.ledgerwallet.walletservice.domain.model.WalletTransaction;

public final class WalletTransactionMapper {
    private WalletTransactionMapper() {
    }

    public static WalletTransactionResponse toResponse(WalletTransaction transaction) {
        return new WalletTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getSourceWalletId(),
                transaction.getDestinationWalletId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getIdempotencyKey(),
                transaction.getPreviousHash(),
                transaction.getCurrentHash(),
                transaction.getNonce(),
                transaction.getDifficulty(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
