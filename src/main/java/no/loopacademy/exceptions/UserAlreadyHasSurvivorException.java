package no.loopacademy.exceptions;

public class UserAlreadyHasSurvivorException extends ResourceConflictException {
    public UserAlreadyHasSurvivorException(String message) {
        super(message);
    }
}
