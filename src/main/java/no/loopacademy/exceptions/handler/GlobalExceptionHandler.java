package no.loopacademy.exceptions.handler;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.exceptions.ResourceNotFoundException;

/**
 * Converts exceptions into HTTP error responses with the shape {status, message}.
 * Picked up automatically by Spring; no need to call or import it.
 *   ResourceNotFoundException        → 404
 *   MethodArgumentNotValidException  → 400 (@Valid failures)
 *   BusinessRuleException            → 400  
 */
@RestControllerAdvice 
public class GlobalExceptionHandler {
    /**
     * Handles ResourceNotFoundException thrown by the service layer when a look-up does not return any existing
     * item. Returns 404 NOT_FOUND response. 
     * @param e exception to handle
     * @return ResponseEntity(status, response)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, Objects.requireNonNullElse(e.getMessage(), "No error message provided")));
    }

    /**
     * Handles error thrown by the controller layer when @Valid fails on a request DTO.
     * Returns 400 BAD_REQUEST response, with invalid fields joined into the message.
     * e.g. {@code name: must not be blank; weight: must be positive}
     * @param e exception to handle. 
     * @return ResponseEntity(status, response)
     */
    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleArgumentNotValid(
        MethodArgumentNotValidException e
    ) {
        
        //For each field, add the name of the field that is wrong to the error message.
        String errorMsg = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(field -> field.getField() + ": " + field.getDefaultMessage())
        .collect(Collectors.joining("; "));

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, errorMsg));
    }

    /**
     * Handles custom BusinessRuleException error thrown (manually) from the service layer.
     * Returns 400 BAD_REQUEST response, and the error message from the exception.
     * @param e exception to handle
     * @return ResponseEntity(status, response)
     */ 
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolated(BusinessRuleException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, Objects.requireNonNullElse(e.getMessage(), "No error message provided")));
    }
}
