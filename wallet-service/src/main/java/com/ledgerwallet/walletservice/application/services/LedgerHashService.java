package com.ledgerwallet.walletservice.application.services;

import com.ledgerwallet.walletservice.application.dtos.LedgerHashResult;
import com.ledgerwallet.walletservice.domain.model.WalletTransaction;

public interface LedgerHashService {
    LedgerHashResult generateHash(WalletTransaction transaction, String previousHash);
}
