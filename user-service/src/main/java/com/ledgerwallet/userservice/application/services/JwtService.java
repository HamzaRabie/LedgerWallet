package com.ledgerwallet.userservice.application.services;

import com.ledgerwallet.userservice.domain.model.User;

public interface JwtService {
    String generateToken(User user);
    boolean validateToken(String token);
    String extractUserId(String token);
    String extractRole(String token);
    
}
