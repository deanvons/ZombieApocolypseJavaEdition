package no.loopacademy.repositories;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import no.loopacademy.models.camp.CampChatVisit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampChatVisitRepository extends JpaRepository<CampChatVisit, UUID> {

    Optional<CampChatVisit> findByIdAndOwner_KeycloakIdAndExpiresAtAfter(
            UUID id, String keycloakId, Instant now);

    int deleteByExpiresAtBefore(Instant now);
}