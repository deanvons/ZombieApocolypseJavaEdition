package no.loopacademy.dtos.response;

public record ActionResponse(
    Long id,
    String name,
    String type,
    String effect,
    String target
) {}
