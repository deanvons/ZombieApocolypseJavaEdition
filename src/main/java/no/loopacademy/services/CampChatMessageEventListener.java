package no.loopacademy.services;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CampChatMessageEventListener {

    private final CampChatStreamRegistry streamRegistry;

    public CampChatMessageEventListener(CampChatStreamRegistry streamRegistry) {
        this.streamRegistry = streamRegistry;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageCommitted(CampMessageCommittedEvent event) {
        streamRegistry.broadcast(event);
    }
}