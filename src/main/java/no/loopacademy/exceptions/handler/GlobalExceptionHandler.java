package no.loopacademy.exceptions.handler;

import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.ResourceNotFoundException;

/**
 * Converts exceptions into HTTP error responses with the shape {status, message}.
 * Picked up automatically by Spring; no need to call or import it.
 *   ResourceNotFoundException        → 404
 *   MethodArgumentNotValidException  → 400 (@Valid failures)
 *   BusinessRuleException            → 400
 *   ResourceConflictException        → 409
 *   Exception (anything else)        → 500
 */
@RestControllerAdvice 
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
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

    /**
     * Handles ResourceConflictException thrown by the service layer when creating something that already exists.
     * Returns 409 CONFLICT response, and the error message from the exception.
     * @param e exception to handle
     * @return ResponseEntity(status, response)
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ResourceConflictException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, Objects.requireNonNullElse(e.getMessage(), "No error message provided")));
    }

    /**
     * Catch-all for any exception not handled by a more specific handler above. Returns 500 INTERNAL_SERVER_ERROR
     * response, so the client still gets the {status, message} shape. Uses a generic message and never
     * e.getMessage(), since internal messages can leak details such as SQL or class names.
     * Also logs the full exception, since it is not logged anywhere else once handled here.
     * @param e exception to handle
     * @param request the failed request, used only for logging the method and path
     * @return ResponseEntity(status, response)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(500, "Internal server error"));
    }
}
