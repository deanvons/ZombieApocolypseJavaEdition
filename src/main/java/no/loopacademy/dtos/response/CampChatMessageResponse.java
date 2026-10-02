package no.loopacademy.dtos.response;

import java.time.Instant;
import java.util.UUID;

public record CampChatMessageResponse(UUID id, String content, String senderName, Instant createdAt) {
}