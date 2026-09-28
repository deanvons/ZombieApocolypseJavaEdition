package no.loopacademy.exceptions;

/**
 * Thrown at the service layer when a request breaks a game rule (e.g. carrying too much).
 * Mapped to 400 BAD_REQUEST by GlobalExceptionHandler.
 * Custom rule exceptions should extend this.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}