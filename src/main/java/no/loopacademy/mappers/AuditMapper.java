package no.loopacademy.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import no.loopacademy.dtos.response.AuditResponse;
import no.loopacademy.models.audit.AuditEntry;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mapping(target = "actorId", source = "actor.id")
    AuditResponse toResponse(AuditEntry auditEntry);

    List<AuditResponse> toResponses(List<AuditEntry> auditEntries);

    
}

