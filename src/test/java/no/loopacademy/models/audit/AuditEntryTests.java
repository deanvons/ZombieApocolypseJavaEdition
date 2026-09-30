package no.loopacademy.models.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import no.loopacademy.models.userprofile.UserProfile;

public class AuditEntryTests {

    @Test
    void constructorShouldSetAllProvidedFields() {
        UserProfile actor = new UserProfile("keycloak-id", "rick");

        AuditEntry entry = new AuditEntry(actor, AuditActionType.SURVIVOR_CREATED, "Survivor", 1L, "Created survivor Rick");

        assertEquals(actor, entry.getActor());
        assertEquals(AuditActionType.SURVIVOR_CREATED, entry.getActionType());
        assertEquals("Survivor", entry.getEntityType());
        assertEquals(1L, entry.getEntityId());
        assertEquals("Created survivor Rick", entry.getDetails());
    }

    @Test
    void timeStampShouldNotBeSetBeforePersisting() {
        UserProfile actor = new UserProfile("keycloak-id", "rick");

        AuditEntry entry = new AuditEntry(actor, AuditActionType.SKILL_ADDED, "Survivor", 1L, null);

        // @CreationTimestamp is populated by Hibernate on insert, not by the constructor
        assertNull(entry.getTimeStamp());
    }
}
