package com.ledgerwallet.walletservice.application.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;
import com.ledgerwallet.walletservice.application.dtos.WithdrawRequest;

import java.util.UUID;

public interface WithdrawService {
    Result<WalletTransactionResponse> withdraw(UUID userId, WithdrawRequest request);
}
