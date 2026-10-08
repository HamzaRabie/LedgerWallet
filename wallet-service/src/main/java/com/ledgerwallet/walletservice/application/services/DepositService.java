package com.ledgerwallet.walletservice.application.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.dtos.DepositRequest;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;

import java.util.UUID;

public interface DepositService {
    Result<WalletTransactionResponse> deposit(UUID userId, DepositRequest request);
}
