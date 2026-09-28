package no.loopacademy.exceptions;

public class SurvivorNotFoundException extends ResourceNotFoundException {
    public SurvivorNotFoundException(String message) {
        super(message);
    }
}
