package no.loopacademy.exceptions;

public class ActionNotFoundException extends ResourceNotFoundException {
    public ActionNotFoundException(String message) {
        super(message);
    }
}
