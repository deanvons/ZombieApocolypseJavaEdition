package no.loopacademy.exceptions;

/**
 * Thrown at the service layer when requested data does not exist.
 * Other custom NotFound exceptions can inherit from this. 
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}