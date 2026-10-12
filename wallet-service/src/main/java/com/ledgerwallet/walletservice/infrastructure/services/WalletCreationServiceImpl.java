package com.ledgerwallet.walletservice.infrastructure.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.dtos.WalletResponse;
import com.ledgerwallet.walletservice.application.mappers.WalletMapper;
import com.ledgerwallet.walletservice.application.services.WalletCreationService;
import com.ledgerwallet.walletservice.domain.model.Wallet;
import com.ledgerwallet.walletservice.infrastructure.persistence.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletCreationServiceImpl implements WalletCreationService {
    private static final String DEFAULT_CURRENCY = "EGP";

    private final WalletRepository walletRepository;

    public WalletCreationServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public Result<WalletResponse> createWalletForUser(UUID userId) {
        var existingWallet = walletRepository.findByUserId(userId);

        if (existingWallet.isPresent()) {
            return Result.success(WalletMapper.toResponse(existingWallet.get()));
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency(DEFAULT_CURRENCY);

        var savedWallet = walletRepository.save(wallet);

        return Result.success(WalletMapper.toResponse(savedWallet));
    }
}
