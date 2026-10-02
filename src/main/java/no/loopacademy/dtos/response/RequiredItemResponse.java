package no.loopacademy.dtos.response;

// type is "tool" or "weapon"; name is null when any item of that type will do
public record RequiredItemResponse(
    String type,
    String name
) {}
