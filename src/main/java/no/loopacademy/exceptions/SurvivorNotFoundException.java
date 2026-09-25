package no.loopacademy.exceptions;

public class SurvivorNotFoundException extends RuntimeException {
    public SurvivorNotFoundException(String message) {
        super(message);
    }
}
