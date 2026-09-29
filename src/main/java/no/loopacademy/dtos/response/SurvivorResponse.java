package no.loopacademy.dtos.response;

import java.util.List;

import no.loopacademy.models.skills.Skill;

public record SurvivorResponse(
    Long id,
    String name,
    String type,
    List<Skill> skills,
    List<ItemResponse> gear
) {}
