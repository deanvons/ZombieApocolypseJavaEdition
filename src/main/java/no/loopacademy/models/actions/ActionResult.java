package no.loopacademy.models.actions;

import no.loopacademy.models.survivors.Survivor;

public record ActionResult(
        Survivor survivor, Action action, double score) {
}
