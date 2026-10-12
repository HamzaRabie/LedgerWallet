package com.ledgerwallet.walletservice.infrastructure.messaging.kafka;

import com.ledgerwallet.walletservice.application.dtos.UserCreatedEvent;
import com.ledgerwallet.walletservice.application.services.WalletCreationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class UserEventsConsumer {
    private static final String USER_CREATED_EVENT_TYPE = "USER_CREATED";

    private final WalletCreationService walletCreationService;
    private final ObjectMapper objectMapper;

    public UserEventsConsumer(WalletCreationService walletCreationService, ObjectMapper objectMapper) {
        this.walletCreationService = walletCreationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-events", groupId = "wallet-service")
    public void consume(String payload) throws Exception {
        var event = objectMapper.readValue(payload, UserCreatedEvent.class);

        if (!USER_CREATED_EVENT_TYPE.equals(event.eventType())) {
            return;
        }

        walletCreationService.createWalletForUser(event.userId());
    }
}
