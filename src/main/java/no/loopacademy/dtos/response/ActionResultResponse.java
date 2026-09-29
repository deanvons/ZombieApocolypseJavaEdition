package no.loopacademy.dtos.response;

public record ActionResultResponse(
    Long survivorId,
    Long actionId,
    double effectiveness
) {}
