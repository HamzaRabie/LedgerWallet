package com.ledgerwallet.walletservice.application.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.dtos.WalletResponse;

import java.util.UUID;

public interface WalletCreationService {
    Result<WalletResponse> createWalletForUser(UUID userId);
}
