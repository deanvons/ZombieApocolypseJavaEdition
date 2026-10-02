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

@Entity
@Table(name = "camp_message", indexes = {
        @Index(name = "idx_camp_message_order", columnList = "camp_id, created_at, id")
})
public class CampMessage {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_survivor_id", nullable = false)
    private Survivor sender;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CampMessage() {
    }

    public CampMessage(Camp camp, Survivor sender, String content, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.camp = camp;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Camp getCamp() {
        return camp;
    }

    public Survivor getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}