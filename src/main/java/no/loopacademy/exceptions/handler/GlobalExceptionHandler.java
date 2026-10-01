package no.loopacademy.exceptions.handler;

import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
 *   AccessDeniedException,
 *   AuthenticationException          → rethrown, Spring Security returns 401/403
 *   Spring MVC exceptions            → their standard status, e.g. 404 unknown path, 405 wrong method
 *   Exception (anything else)        → 500
 */
@RestControllerAdvice 
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
     * Overrides the hook in ResponseEntityExceptionHandler, which already has an @ExceptionHandler for this
     * exception. Adding our own @ExceptionHandler for it would make Spring fail on startup (ambiguous handler).
     * @param ex exception to handle
     * @param headers headers from ResponseEntityExceptionHandler
     * @param status status from ResponseEntityExceptionHandler (400)
     * @param request the current request
     * @return ResponseEntity(status, response)
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        //For each field, add the name of the field that is wrong to the error message.
        String errorMsg = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(field -> field.getField() + ": " + field.getDefaultMessage())
        .collect(Collectors.joining("; "));

        return ResponseEntity
            .status(status)
            .headers(headers)
            .body(new ErrorResponse(status.value(), errorMsg));
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
     * Rethrows Spring Security exceptions (e.g. from @PreAuthorize) so they reach Spring Security's own filters,
     * which turn them into 401 UNAUTHORIZED or 403 FORBIDDEN. Without this, the catch-all below would turn them
     * into 500. Re-throwing the same exception tells Spring that this handler does not handle it.
     * @param e exception to rethrow
     */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public void handleSecurityException(RuntimeException e) {
        throw e;
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

    /**
     * Handles Spring MVC's own exceptions (e.g. wrong HTTP method, unknown path, malformed JSON, wrong type in the
     * path), which ResponseEntityExceptionHandler gives their standard status (400/404/405/415...). It builds the
     * response first, and we convert its ProblemDetail body into our {status, message} shape.
     * Uses the ProblemDetail's detail as the message, never ex.getMessage(), since that can contain class names
     * and parser details. Keeps the headers, e.g. Allow on 405 and Accept on 415.
     * @param ex exception to handle
     * @param body ProblemDetail from ResponseEntityExceptionHandler, or null if it should create one
     * @param headers headers to keep in the response
     * @param statusCode status from ResponseEntityExceptionHandler
     * @param request the current request
     * @return ResponseEntity(status, response), or null if the response is already committed
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response == null) {
            return null;
        }

        String message = response.getBody() instanceof ProblemDetail problemDetail && problemDetail.getDetail() != null
            ? problemDetail.getDetail()
            : "Request could not be processed";

        return ResponseEntity
            .status(response.getStatusCode())
            .headers(response.getHeaders())
            .body(new ErrorResponse(response.getStatusCode().value(), message));
    }
}
