package no.loopacademy.repositories;

import java.util.Optional;

import no.loopacademy.models.userprofile.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // Spring Data builds the queries from the method names
    Optional<UserProfile> findByKeycloakId(String keycloakId);

    boolean existsByKeycloakId(String keycloakId);
}
