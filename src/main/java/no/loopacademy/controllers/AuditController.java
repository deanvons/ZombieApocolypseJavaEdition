package no.loopacademy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.loopacademy.dtos.response.AuditResponse;
import no.loopacademy.mappers.AuditMapper;
import no.loopacademy.models.audit.AuditEntry;
import no.loopacademy.services.AuditEntryService;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEntryService auditService;
    private final AuditMapper auditMapper;

    public AuditController(AuditEntryService auditService, AuditMapper auditMapper) {
        this.auditService = auditService;
        this.auditMapper = auditMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditResponse>> getAllAuditEntries(@AuthenticationPrincipal Jwt jwt) {
        List<AuditEntry> auditEntries = auditService.findAll();
        return ResponseEntity.ok(auditMapper.toResponses(auditEntries));
    }


}

