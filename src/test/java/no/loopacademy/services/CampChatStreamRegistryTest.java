package no.loopacademy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import no.loopacademy.dtos.response.CampChatMessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class CampChatStreamRegistryTest {

    @Test
    void broadcastOnlySendsToMatchingCampVisitsAlreadyStarted() {
        TestRegistry registry = new TestRegistry();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(3600);
        UUID visitId = UUID.randomUUID();
        RecordingEmitter active = registry.add(visitId, "shared", now.minusSeconds(1), expiry);
        RecordingEmitter notStarted = registry.add(UUID.randomUUID(), "shared", now.plusSeconds(1), expiry);
        RecordingEmitter otherCamp = registry.add(UUID.randomUUID(), "other", now.minusSeconds(1), expiry);
        CampChatMessageResponse message = new CampChatMessageResponse(
                UUID.randomUUID(), "hello", "Mara", now);

        registry.broadcast(new CampMessageCommittedEvent("shared", message));

        assertEquals(1, active.sent);
        assertEquals(0, notStarted.sent);
        assertEquals(0, otherCamp.sent);
    }

    private static class TestRegistry extends CampChatStreamRegistry {
        @Override
        protected SseEmitter createEmitter(long timeoutMillis) {
            return new RecordingEmitter();
        }

        RecordingEmitter add(UUID visitId, String campId, Instant startedAt, Instant expiresAt) {
            return (RecordingEmitter) register(visitId, campId, startedAt, expiresAt);
        }
    }

    private static class RecordingEmitter extends SseEmitter {
        private int sent;

        @Override
        public void send(@org.springframework.lang.NonNull SseEventBuilder builder) throws IOException {
            sent++;
        }
    }
}