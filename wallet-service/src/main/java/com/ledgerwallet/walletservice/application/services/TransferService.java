package com.ledgerwallet.walletservice.application.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.dtos.TransferRequest;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;

import java.util.UUID;

public interface TransferService {
    Result<WalletTransactionResponse> transfer(UUID senderUserId, TransferRequest request);
}
