package no.loopacademy.exceptions.handler;

/**
 * All error response bodies returned to the client should have the same JSON shape {status, message}.
 * i.e. {"status": 400, "message": "name: must not be blank"}
 */
public record ErrorResponse(int status, String message) {}