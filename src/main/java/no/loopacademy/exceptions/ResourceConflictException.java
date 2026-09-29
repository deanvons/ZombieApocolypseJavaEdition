package no.loopacademy.exceptions;

/**
 * Thrown at the service layer when creating something that already exists
 * (e.g. a second profile for the same user). Mapped to 409 CONFLICT by GlobalExceptionHandler.
 * Other custom conflict exceptions can inherit from this.
 */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
