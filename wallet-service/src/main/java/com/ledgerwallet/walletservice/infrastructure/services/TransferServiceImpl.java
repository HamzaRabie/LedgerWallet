package com.ledgerwallet.walletservice.infrastructure.services;

import com.ledgerwallet.walletservice.application.common.Result;
import com.ledgerwallet.walletservice.application.common.WalletErrors;
import com.ledgerwallet.walletservice.application.dtos.TransferRequest;
import com.ledgerwallet.walletservice.application.dtos.WalletTransactionResponse;
import com.ledgerwallet.walletservice.application.mappers.WalletTransactionMapper;
import com.ledgerwallet.walletservice.application.services.LedgerHashService;
import com.ledgerwallet.walletservice.application.services.TransferService;
import com.ledgerwallet.walletservice.domain.model.TransactionStatus;
import com.ledgerwallet.walletservice.domain.model.TransactionType;
import com.ledgerwallet.walletservice.domain.model.WalletTransaction;
import com.ledgerwallet.walletservice.infrastructure.persistence.repository.WalletRepository;
import com.ledgerwallet.walletservice.infrastructure.persistence.repository.WalletTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {
    private static final int MAX_OPTIMISTIC_LOCK_RETRIES = 3;

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final LedgerHashService ledgerHashService;
    private final TransactionTemplate transactionTemplate;

    public TransferServiceImpl(
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
    public Result<WalletTransactionResponse> transfer(UUID senderUserId, TransferRequest request) {
        if (senderUserId.equals(request.receiverUserId())) {
            return Result.failure(WalletErrors.sameWalletTransfer());
        }

        var existingTransaction = walletTransactionRepository.findByIdempotencyKey(request.idempotencyKey());

        if (existingTransaction.isPresent()) {
            var response = WalletTransactionMapper.toResponse(existingTransaction.get());
            return Result.success(response);
        }

        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_LOCK_RETRIES; attempt++) {
            try {
                return transactionTemplate.execute(status -> {
                    var senderWalletResult = walletRepository.findByUserId(senderUserId);
                    if (senderWalletResult.isEmpty()) return Result.failure(WalletErrors.walletNotFound());

                    var receiverWalletResult = walletRepository.findByUserId(request.receiverUserId());
                    if (receiverWalletResult.isEmpty()) return Result.failure(WalletErrors.walletNotFound());

                    var senderWallet = senderWalletResult.get();
                    var receiverWallet = receiverWalletResult.get();

                    if (senderWallet.getBalance().compareTo(request.amount()) < 0) {
                        return Result.failure(WalletErrors.insufficientBalance());
                    }

                    senderWallet.setBalance(senderWallet.getBalance().subtract(request.amount()));
                    receiverWallet.setBalance(receiverWallet.getBalance().add(request.amount()));

                    walletRepository.saveAndFlush(senderWallet);
                    walletRepository.saveAndFlush(receiverWallet);

                    WalletTransaction transaction = new WalletTransaction();
                    transaction.setType(TransactionType.TRANSFER);
                    transaction.setSourceWalletId(senderWallet.getId());
                    transaction.setDestinationWalletId(receiverWallet.getId());
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
                if (attempt == MAX_OPTIMISTIC_LOCK_RETRIES) {
                    return Result.failure(WalletErrors.concurrentWalletUpdate());
                }
            } catch (DataIntegrityViolationException ex) {
                return walletTransactionRepository.findByIdempotencyKey(request.idempotencyKey())
                        .map(WalletTransactionMapper::toResponse)
                        .<Result<WalletTransactionResponse>>map(Result::success)
                        .orElseGet(() -> Result.failure(WalletErrors.duplicateIdempotencyKey()));
            }
        }

        return Result.failure(WalletErrors.concurrentWalletUpdate());
    }
}
