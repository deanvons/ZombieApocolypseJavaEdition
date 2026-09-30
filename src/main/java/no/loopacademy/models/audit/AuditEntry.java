package no.loopacademy.models.audit;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import no.loopacademy.models.userprofile.UserProfile;

@Entity 
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserProfile actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType actionType;

    @Column(nullable = false)
    private String entityType; //I.e. "Survivor"

    @Column(nullable = false)
    private Long entityId;

    private String details;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp 
    private LocalDateTime timeStamp;

    protected AuditEntry() {}

    public AuditEntry(UserProfile actor, AuditActionType actionType, String entityType, Long entityId, String details) {
        this.actor = actor;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public UserProfile getActor() {
        return actor;
    }

    public AuditActionType getActionType() {
        return actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }
}
