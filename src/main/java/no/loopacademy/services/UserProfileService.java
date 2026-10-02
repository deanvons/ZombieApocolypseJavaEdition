package no.loopacademy.services;

import no.loopacademy.exceptions.ResourceConflictException;
import no.loopacademy.exceptions.UserProfileNotFoundException;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.UserProfileRepository;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * Profiles are always looked up by Keycloak id (the JWT "sub" claim), because
 * that's all we know about the caller after login. The controller takes it
 * from the verified token, never from the request, so a user can only ever
 * reach their own profile.
 */
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfile create(String keycloakId, String displayName) {
        if (userProfileRepository.existsByKeycloakId(keycloakId)) {
            throw new ResourceConflictException("A profile already exists for this user");
        }
        try {
            // saveAndFlush so a unique-constraint violation surfaces here, not at commit
            return userProfileRepository.saveAndFlush(new UserProfile(keycloakId, displayName));
        } catch (DataIntegrityViolationException e) {
            // Two simultaneous first requests: both passed the check above, the database stopped the second
            throw new ResourceConflictException("A profile already exists for this user");
        }
    }

    @Transactional(readOnly = true)
    public UserProfile findByKeycloakId(String keycloakId) {
        return userProfileRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserProfileNotFoundException("No profile found for this user"));
    }

    @Transactional(readOnly = true)
    public List<UserProfile> findAll() {
        return userProfileRepository.findAll();
    }

}
