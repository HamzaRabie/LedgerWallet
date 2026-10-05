package com.ledgerwallet.userservice.infrastructure.persistence.repository;

import com.ledgerwallet.userservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User , UUID> {
}
