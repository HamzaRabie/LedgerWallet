package com.ledgerwallet.userservice.application.dtos;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID userId,
        String email,
        String username
) {
}
