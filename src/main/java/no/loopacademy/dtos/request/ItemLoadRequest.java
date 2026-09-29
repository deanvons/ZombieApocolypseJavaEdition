package no.loopacademy.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// type is "tool" or "weapon"; durability is required for tools, damage for weapons
public record ItemLoadRequest(
    @NotBlank
    String type,
    
    @NotBlank
    String name,
    
    @NotNull
    @Positive
    Double weight,

    @Positive
    Double durability,

    @Positive 
    Double damage
) {}
