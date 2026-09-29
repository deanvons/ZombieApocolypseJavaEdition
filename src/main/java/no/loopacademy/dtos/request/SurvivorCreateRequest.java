package no.loopacademy.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record SurvivorCreateRequest(
    @NotBlank
    String name,
    
    @NotBlank
    String type // mapped to SurvivorType by SurvivorMapper, case-insensitive
) {}
