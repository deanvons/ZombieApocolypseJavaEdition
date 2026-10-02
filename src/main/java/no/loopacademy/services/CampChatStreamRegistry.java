package no.loopacademy.services;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class CampChatStreamRegistry {

    private final ConcurrentMap<UUID, ConcurrentMap<UUID, Subscription>> subscriptions = new ConcurrentHashMap<>();

    public SseEmitter register(UUID visitId, String campId, Instant startedAt, Instant expiresAt) {
        long timeoutMillis = Math.max(1, Duration.between(Instant.now(), expiresAt).toMillis());
        SseEmitter emitter = createEmitter(timeoutMillis);
        UUID connectionId = UUID.randomUUID();
        Subscription subscription = new Subscription(connectionId, campId, startedAt, emitter);
        subscriptions.computeIfAbsent(visitId, ignored -> new ConcurrentHashMap<>()).put(connectionId, subscription);
        Runnable cleanup = () -> remove(visitId, connectionId);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ignored -> cleanup.run());
        return emitter;
    }

    public void broadcast(CampMessageCommittedEvent event) {
        var message = Objects.requireNonNull(event.message());
        Instant createdAt = message.createdAt();
        subscriptions.values().forEach(visitSubscriptions -> visitSubscriptions.values().forEach(subscription -> {
            if (!subscription.campId().equals(event.campId()) || createdAt.isBefore(subscription.startedAt())) {
                return;
            }
            try {
                subscription.emitter().send(SseEmitter.event()
                    .id(Objects.requireNonNull(CampChatService.encodeCursor(message)))
                        .name("message.created")
                    .data(message));
            } catch (IOException exception) {
                removeByConnection(subscription.connectionId());
            }
        }));
    }

    protected SseEmitter createEmitter(long timeoutMillis) {
        return new SseEmitter(timeoutMillis);
    }

    private void remove(UUID visitId, UUID connectionId) {
        subscriptions.computeIfPresent(visitId, (ignored, visitSubscriptions) -> {
            visitSubscriptions.remove(connectionId);
            return visitSubscriptions.isEmpty() ? null : visitSubscriptions;
        });
    }

    private void removeByConnection(UUID connectionId) {
        subscriptions.forEach((visitId, visitSubscriptions) -> {
            if (visitSubscriptions.containsKey(connectionId)) {
                remove(visitId, connectionId);
            }
        });
    }

    private record Subscription(
            UUID connectionId,
            String campId,
            Instant startedAt,
            SseEmitter emitter) {
    }
}