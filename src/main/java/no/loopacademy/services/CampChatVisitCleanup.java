package no.loopacademy.services;

import java.time.Instant;

import no.loopacademy.repositories.CampChatVisitRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CampChatVisitCleanup {

    private final CampChatVisitRepository visitRepository;

    public CampChatVisitCleanup(CampChatVisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void deleteExpiredVisits() {
        visitRepository.deleteByExpiresAtBefore(Instant.now());
    }
}