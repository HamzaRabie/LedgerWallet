package com.ledgerwallet.walletservice.application.mappers;

import com.ledgerwallet.walletservice.application.dtos.WalletResponse;
import com.ledgerwallet.walletservice.domain.model.Wallet;

public final class WalletMapper {
    private WalletMapper() {
    }

    public static WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getVersion(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
