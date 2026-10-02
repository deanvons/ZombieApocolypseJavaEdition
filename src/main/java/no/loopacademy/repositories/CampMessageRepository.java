package no.loopacademy.repositories;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import no.loopacademy.models.camp.CampMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampMessageRepository extends JpaRepository<CampMessage, UUID> {

    @Query("""
            select message from CampMessage message
            where message.camp.id = :campId
              and message.createdAt >= :startedAt
              and (:cursorTime is null
                   or message.createdAt > :cursorTime
                   or (message.createdAt = :cursorTime and message.id > :cursorId))
            order by message.createdAt asc, message.id asc
            """)
    List<CampMessage> findVisitMessages(
            @Param("campId") String campId,
            @Param("startedAt") Instant startedAt,
            @Param("cursorTime") Instant cursorTime,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);
}