package no.loopacademy.dtos.request;

import no.loopacademy.models.survivors.SurvivorType;

public record CreateSurvivorRequest(String name, SurvivorType type) {
}
