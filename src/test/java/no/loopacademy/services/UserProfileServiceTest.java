package no.loopacademy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.UserProfileNotFoundException;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.UserProfileRepository;

class UserProfileServiceTest {

    private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    private UserProfileRepository repository;
    private UserProfileService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserProfileRepository.class);
        service = new UserProfileService(repository);
    }

    @Test
    void createShouldSaveNewProfile() {
        when(repository.existsByKeycloakId(KEYCLOAK_ID)).thenReturn(false);
        when(repository.saveAndFlush(any(UserProfile.class))).thenAnswer(call -> call.getArgument(0));

        UserProfile profile = service.create(KEYCLOAK_ID, "rick");

        assertEquals(KEYCLOAK_ID, profile.getKeycloakId());
        assertEquals("rick", profile.getDisplayName());
    }

    @Test
    void createWhenProfileExistsShouldThrowConflict() {
        when(repository.existsByKeycloakId(KEYCLOAK_ID)).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> service.create(KEYCLOAK_ID, "rick"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void createWhenDatabaseRejectsDuplicateShouldThrowConflict() {
        // Simulates two simultaneous first requests: the check passes, the unique constraint catches it
        when(repository.existsByKeycloakId(KEYCLOAK_ID)).thenReturn(false);
        when(repository.saveAndFlush(any(UserProfile.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(ResourceConflictException.class, () -> service.create(KEYCLOAK_ID, "rick"));
    }

    @Test
    void findByKeycloakIdShouldReturnProfile() {
        UserProfile profile = new UserProfile(KEYCLOAK_ID, "rick");
        when(repository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(profile));

        assertEquals(profile, service.findByKeycloakId(KEYCLOAK_ID));
    }

    @Test
    void findByKeycloakIdWhenMissingShouldThrowNotFound() {
        when(repository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());

        assertThrows(UserProfileNotFoundException.class, () -> service.findByKeycloakId(KEYCLOAK_ID));
    }
}
