package no.loopacademy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.exceptions.ResourceNotFoundException;
import no.loopacademy.models.audit.AuditActionType;
import no.loopacademy.models.audit.AuditEntry;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.AuditEntryRepository;

class AuditEntryServiceTest {

    private AuditEntryRepository repository;
    private AuditEntryService service;
    private UserProfile actor;
    private String entityType;
    private Long entityId;

    @BeforeEach
    void setUp() {
        String actorKeycloakId = "generic-keycloak-id";
        String actorDisplayName = "GenericActorName";
        entityType = "Survivor";
        entityId = 1L;
        repository = mock(AuditEntryRepository.class);
        service = new AuditEntryService(repository);
        actor = new UserProfile(actorKeycloakId, actorDisplayName);
    }

    @Test
    void createShouldSaveAndReturnEntry() {
        when(repository.save(any(AuditEntry.class))).thenAnswer(call -> call.getArgument(0));

        String details = "Created survivor Rick";
        AuditEntry entry = service.create(actor, AuditActionType.SURVIVOR_CREATED, entityType, entityId, details);

        assertEquals(actor, entry.getActor());
        assertEquals(AuditActionType.SURVIVOR_CREATED, entry.getActionType());
        assertEquals(entityType, entry.getEntityType());
        assertEquals(entityId, entry.getEntityId());
    }

    @Test
    void createWithoutActorShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(null, AuditActionType.SURVIVOR_CREATED, entityType, entityId, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void createWithoutActionTypeShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(actor, null, entityType, entityId, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void createWithBlankEntityTypeShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(actor, AuditActionType.SURVIVOR_CREATED, "  ", entityId, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void createWithoutEntityIdShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(actor, AuditActionType.SURVIVOR_CREATED, entityType, null, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void findAllShouldReturnAllEntries() {
        AuditEntry entry = new AuditEntry(actor, AuditActionType.SKILL_ADDED, entityType, entityId, "details");
        when(repository.findAll()).thenReturn(List.of(entry));

        assertEquals(List.of(entry), service.findAll());
    }

    @Test
    void findByIdShouldReturnEntry() {
        AuditEntry entry = new AuditEntry(actor, AuditActionType.SKILL_ADDED, "Survivor", 1L, "details");
        when(repository.findById(entityId)).thenReturn(Optional.of(entry));

        assertEquals(entry, service.findById(entityId));
    }

    @Test
    void findByIdWhenMissingShouldThrowNotFound() {
        when(repository.findById(entityId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(entityId));
    }
}
