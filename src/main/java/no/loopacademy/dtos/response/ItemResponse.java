package no.loopacademy.dtos.response;

// durability is only set for tools, damage only for weapons
public record ItemResponse(
    Long id,
    String type,
    String name,
    Double weight,
    Double durability,
    Double damage
) {}
