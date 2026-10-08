package com.ledgerwallet.walletservice.infrastructure.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.common.WalletErrors;
import com.ledgerwallet.walletservice.application.dtos.DepositRequest;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;
import com.ledgerwallet.walletservice.application.mappers.WalletTransactionMapper;
import com.ledgerwallet.walletservice.application.services.DepositService;
import com.ledgerwallet.walletservice.application.services.LedgerHashService;
import com.ledgerwallet.walletservice.domain.model.TransactionStatus;
import com.ledgerwallet.walletservice.domain.model.TransactionType;
import com.ledgerwallet.walletservice.domain.model.WalletTransaction;
import com.ledgerwallet.walletservice.infrastructure.persistence.repository.WalletTransactionRepository;
import com.ledgerwallet.walletservice.infrastructure.persistence.repository.WalletRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
public class DepositServiceImpl implements DepositService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final LedgerHashService ledgerHashService;
    private final TransactionTemplate transactionTemplate;

    public DepositServiceImpl(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            LedgerHashService ledgerHashService,
            PlatformTransactionManager transactionManager
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.ledgerHashService = ledgerHashService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public Result<WalletTransactionResponse> deposit(UUID userId, DepositRequest request) {
        var existingTransaction = walletTransactionRepository.findByIdempotencyKey(request.idempotencyKey());

        if (existingTransaction.isPresent()) {
            var response = WalletTransactionMapper.toResponse(existingTransaction.get());
            return Result.success(response);
        }

        try {
            return transactionTemplate.execute(status -> {
                var userWallet = walletRepository.findByUserId(userId);
                if(userWallet.isEmpty())return Result.failure(WalletErrors.walletNotFound());

                var wallet = userWallet.get();
                wallet.setBalance(wallet.getBalance().add(request.amount()));
                walletRepository.saveAndFlush(wallet);

                WalletTransaction transaction = new WalletTransaction();
                transaction.setType(TransactionType.DEPOSIT);
                transaction.setSourceWalletId(null);
                transaction.setDestinationWalletId(wallet.getId());
                transaction.setAmount(request.amount());
                transaction.setStatus(TransactionStatus.COMPLETED);
                transaction.setIdempotencyKey(request.idempotencyKey());
                var previousHash = walletTransactionRepository.findTopByOrderByCreatedAtDesc()
                        .map(WalletTransaction::getCurrentHash)
                        .orElse(null);

                transaction.setPreviousHash(previousHash);

                var hashResult = ledgerHashService.generateHash(transaction, transaction.getPreviousHash());
                transaction.setCurrentHash(hashResult.hash());
                transaction.setNonce(hashResult.nonce());
                transaction.setDifficulty(hashResult.difficulty());

                var savedTransaction = walletTransactionRepository.saveAndFlush(transaction);
                var response = WalletTransactionMapper.toResponse(savedTransaction);

                return Result.success(response);
            });
        } catch (ObjectOptimisticLockingFailureException ex) {
            return Result.failure(WalletErrors.concurrentWalletUpdate());
        } catch (DataIntegrityViolationException ex) {
            return walletTransactionRepository.findByIdempotencyKey(request.idempotencyKey())
                    .map(WalletTransactionMapper::toResponse)
                    .<Result<WalletTransactionResponse>>map(Result::success)
                    .orElseGet(() -> Result.failure(WalletErrors.duplicateIdempotencyKey()));
        }
    }
}
