package no.loopacademy.dtos.response;

import java.time.Instant;

// survivorId is null until the player has created their survivor
public record UserProfileResponse(
    Long id,
    String displayName,
    Instant createdAt,
    Long survivorId
) {}
