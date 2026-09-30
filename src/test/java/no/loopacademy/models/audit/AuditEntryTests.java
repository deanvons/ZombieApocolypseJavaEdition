package no.loopacademy.models.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.models.userprofile.UserProfile;

public class AuditEntryTests {
    private UserProfile actor;
    private String entityType;
    private Long entityId;

    @BeforeEach
    void setup() {
        String actorKeycloakId = "generic-keycloak-id";
        String actorDisplayName = "GenericActorName";
        actor = new UserProfile(actorKeycloakId, actorDisplayName);
        entityType = "Survivor";
        entityId = 1L;
    }

    @Test
    void constructorShouldSetAllProvidedFields() {
        String details = "Created survivor Rick";
        AuditEntry entry = new AuditEntry(actor, AuditActionType.SURVIVOR_CREATED, entityType, entityId, details);

        assertEquals(actor, entry.getActor());
        assertEquals(AuditActionType.SURVIVOR_CREATED, entry.getActionType());
        assertEquals(entityType, entry.getEntityType());
        assertEquals(entityId, entry.getEntityId());
        assertEquals(details, entry.getDetails());
    }

    @Test
    void timeStampShouldNotBeSetBeforePersisting() {
        AuditEntry entry = new AuditEntry(actor, AuditActionType.SKILL_ADDED, entityType, entityId, null);

        // @CreationTimestamp is populated by Hibernate on insert, not by the constructor
        assertNull(entry.getTimeStamp());
    }
}
