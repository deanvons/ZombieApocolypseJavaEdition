package no.loopacademy.exceptions;

/**
 * Thrown at the service layer when a user acts on a resource they do not own
 * (e.g. adding a skill to someone else's survivor). Mapped to 403 FORBIDDEN by GlobalExceptionHandler.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}