package no.loopacademy.exceptions.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.ResourceNotFoundException;
import no.loopacademy.exceptions.SurvivorNotFoundException;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound_survivorNotFoundException_shouldReturn404WithMessage() {
        // ARRANGE
        SurvivorNotFoundException exception = new SurvivorNotFoundException("Survivor with id: 99 not found");
        ErrorResponse expectedBody = new ErrorResponse(404, "Survivor with id: 99 not found");

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception);

        // ASSERT
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleNotFound_nullMessage_shouldReturnFallbackMessage() {
        // ARRANGE
        ResourceNotFoundException exception = new ResourceNotFoundException(null);
        ErrorResponse expectedBody = new ErrorResponse(404, "No error message provided");

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception);

        // ASSERT
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleBusinessRuleViolated_overloadedException_shouldReturn400WithMessage() {
        // ARRANGE
        OverloadedException exception = new OverloadedException("Survivor is overloaded");
        ErrorResponse expectedBody = new ErrorResponse(400, "Survivor is overloaded");

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleBusinessRuleViolated(exception);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleArgumentNotValid_twoInvalidFields_shouldReturn400WithJoinedMessage() {
        // ARRANGE
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "must not be blank"));
        bindingResult.addError(new FieldError("request", "weight", "must be positive"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        ErrorResponse expectedBody = new ErrorResponse(400, "name: must not be blank; weight: must be positive");

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleArgumentNotValid(exception);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleGeneric_nullPointerException_shouldReturn500WithGenericMessage() {
        // ARRANGE
        NullPointerException exception = new NullPointerException();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/survivors/1");
        ErrorResponse expectedBody = new ErrorResponse(500, "Internal server error");

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(exception, request);

        // ASSERT
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleGeneric_runtimeExceptionWithSecret_shouldReturn500WithGenericMessage() {
        // ARRANGE
        RuntimeException exception = new RuntimeException("Failed to run SQL: select * from survivor");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/survivors");
        ErrorResponse expectedBody = new ErrorResponse(500, "Internal server error");

        // ACT
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(exception, request);

        // ASSERT
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }
}
