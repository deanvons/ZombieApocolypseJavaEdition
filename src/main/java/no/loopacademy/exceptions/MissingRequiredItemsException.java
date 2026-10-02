package no.loopacademy.exceptions;

public class MissingRequiredItemsException extends BusinessRuleException {
    public MissingRequiredItemsException(String message) {
        super(message);
    }
}
