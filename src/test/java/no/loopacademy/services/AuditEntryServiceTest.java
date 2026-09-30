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

    @BeforeEach
    void setUp() {
        repository = mock(AuditEntryRepository.class);
        service = new AuditEntryService(repository);
        actor = new UserProfile("keycloak-id", "rick");
    }

    @Test
    void createShouldSaveAndReturnEntry() {
        when(repository.save(any(AuditEntry.class))).thenAnswer(call -> call.getArgument(0));

        AuditEntry entry = service.create(actor, AuditActionType.SURVIVOR_CREATED, "Survivor", 1L, "Created survivor Rick");

        assertEquals(actor, entry.getActor());
        assertEquals(AuditActionType.SURVIVOR_CREATED, entry.getActionType());
        assertEquals("Survivor", entry.getEntityType());
        assertEquals(1L, entry.getEntityId());
    }

    @Test
    void createWithoutActorShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(null, AuditActionType.SURVIVOR_CREATED, "Survivor", 1L, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void createWithoutActionTypeShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(actor, null, "Survivor", 1L, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void createWithBlankEntityTypeShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(actor, AuditActionType.SURVIVOR_CREATED, "  ", 1L, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void createWithoutEntityIdShouldThrowBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
            () -> service.create(actor, AuditActionType.SURVIVOR_CREATED, "Survivor", null, "details"));
        verify(repository, never()).save(any());
    }

    @Test
    void findAllShouldReturnAllEntries() {
        AuditEntry entry = new AuditEntry(actor, AuditActionType.SKILL_ADDED, "Survivor", 1L, "details");
        when(repository.findAll()).thenReturn(List.of(entry));

        assertEquals(List.of(entry), service.findAll());
    }

    @Test
    void findByIdShouldReturnEntry() {
        AuditEntry entry = new AuditEntry(actor, AuditActionType.SKILL_ADDED, "Survivor", 1L, "details");
        when(repository.findById(1L)).thenReturn(Optional.of(entry));

        assertEquals(entry, service.findById(1L));
    }

    @Test
    void findByIdWhenMissingShouldThrowNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }
}
