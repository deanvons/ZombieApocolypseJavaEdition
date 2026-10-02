package no.loopacademy.models.camp;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.userprofile.UserProfile;

@Entity
@Table(name = "camp_chat_visit", indexes = {
        @Index(name = "idx_camp_visit_owner_expiry", columnList = "owner_profile_id, expires_at"),
        @Index(name = "idx_camp_visit_camp_start", columnList = "camp_id, started_at")
})
public class CampChatVisit {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_profile_id", nullable = false)
    private UserProfile owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survivor_id", nullable = false)
    private Survivor survivor;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected CampChatVisit() {
    }

    public CampChatVisit(Camp camp, UserProfile owner, Survivor survivor, Instant startedAt, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.camp = camp;
        this.owner = owner;
        this.survivor = survivor;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public Camp getCamp() {
        return camp;
    }

    public UserProfile getOwner() {
        return owner;
    }

    public Survivor getSurvivor() {
        return survivor;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}