package no.loopacademy.dtos.response;

import java.util.List;

public record ActionResponse(
    Long id,
    String name,
    String type,
    String effect,
    String target,
    List<RequiredItemResponse> requiredItems
) {}
