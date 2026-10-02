package no.loopacademy.exceptions.handler;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import no.loopacademy.exceptions.OverloadedException;
import no.loopacademy.exceptions.ForbiddenException;
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
    void testHandleMethodArgumentNotValid_twoInvalidFields_shouldReturn400WithJoinedMessage() {
        // ARRANGE
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "must not be blank"));
        bindingResult.addError(new FieldError("request", "weight", "must be positive"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);
        ErrorResponse expectedBody = new ErrorResponse(400, "name: must not be blank; weight: must be positive");

        // ACT
        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
            exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest())
        );

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleException_messageNotReadableWithParserDetails_shouldReturn400WithProblemDetailMessage() throws Exception {
        // ARRANGE
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
            "JSON parse error: Unexpected character ('n' (code 110))", new MockHttpInputMessage("{not json".getBytes())
        );
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("POST", "/api/survivors"));
        ErrorResponse expectedBody = new ErrorResponse(400, "Failed to read request");

        // ACT
        ResponseEntity<Object> response = handler.handleException(exception, request);

        // ASSERT
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleForbidden_shouldReturn403WithMessage() {
        ForbiddenException exception = new ForbiddenException("You do not own this survivor");
        ErrorResponse expectedBody = new ErrorResponse(403, "You do not own this survivor");

        ResponseEntity<ErrorResponse> response = handler.handleForbidden(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
    }

    @Test
    void testHandleException_methodNotSupported_shouldReturn405WithAllowHeader() throws Exception {
        // ARRANGE
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("PUT", List.of("GET"));
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("PUT", "/api/actions/1"));
        ErrorResponse expectedBody = new ErrorResponse(405, "Method 'PUT' is not supported.");

        // ACT
        ResponseEntity<Object> response = handler.handleException(exception, request);

        // ASSERT
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(expectedBody, response.getBody());
        assertEquals(Set.of(HttpMethod.GET), response.getHeaders().getAllow());
    }

    @Test
    void testHandleSecurityException_accessDeniedException_shouldRethrowSameException() {
        // ARRANGE
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // ACT + ASSERT
        AccessDeniedException thrown = assertThrows(AccessDeniedException.class, () -> { 
            handler.handleSecurityException(exception);
        });
        assertSame(exception, thrown);
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
