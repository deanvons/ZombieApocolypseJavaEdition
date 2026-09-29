package no.loopacademy.dtos.request;

import jakarta.validation.constraints.NotNull;

import no.loopacademy.models.skills.Skill;

public record SkillAddRequest(
    @NotNull
    Skill skill
) {}
