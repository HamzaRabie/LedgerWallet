package com.ledgerwallet.walletservice.infrastructure.persistence.repository;

import com.ledgerwallet.walletservice.domain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserId(UUID userId);
}
