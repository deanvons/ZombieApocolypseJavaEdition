package no.loopacademy.repositories;

import no.loopacademy.models.audit.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEntryRepository extends JpaRepository<AuditEntry,Long> {
}

