package no.loopacademy.dtos.response;

import java.time.LocalDateTime;

import no.loopacademy.models.audit.AuditActionType;

public record AuditResponse(
    Long id,
    Long actorId,
    AuditActionType actionType,
    String entityType,
    Long entityId,
    String details,
    LocalDateTime timeStamp
) {}
