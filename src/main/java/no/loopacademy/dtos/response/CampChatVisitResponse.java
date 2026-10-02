package no.loopacademy.dtos.response;

import java.time.Instant;
import java.util.UUID;

public record CampChatVisitResponse(UUID visitId, Instant startedAt) {
}