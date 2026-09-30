package no.loopacademy.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.exceptions.ResourceNotFoundException;
import no.loopacademy.models.audit.AuditActionType;
import no.loopacademy.models.audit.AuditEntry;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.AuditEntryRepository;

@Service
public class AuditEntryService {

    private final AuditEntryRepository auditEntryRepository;

    public AuditEntryService(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    @Transactional
    public AuditEntry create(UserProfile actor, AuditActionType actionType, String entityType, Long entityId, String details) {
        if (actor == null) {
            throw new BusinessRuleException("actor is required");
        }
        if (actionType == null) {
            throw new BusinessRuleException("actionType is required");
        }
        if (entityType == null || entityType.isBlank()) {
            throw new BusinessRuleException("entityType is required");
        }
        if (entityId == null) {
            throw new BusinessRuleException("entityId is required");
        }
        return auditEntryRepository.save(new AuditEntry(actor, actionType, entityType, entityId, details));
    }

    @Transactional(readOnly = true)
    public List<AuditEntry> findAll() {
        return auditEntryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AuditEntry findById(Long id) {
        return auditEntryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Audit entry not found"));
    }
}
