package no.loopacademy.exceptions;

public class UserProfileNotFoundException extends ResourceNotFoundException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}
