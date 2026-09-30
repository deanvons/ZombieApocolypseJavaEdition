package no.loopacademy.models.userprofile;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import no.loopacademy.models.survivors.Survivor;

/*
 * The game's view of a logged-in user. Keycloak owns identity (login, password,
 * email); we only store what the game needs, linked to the Keycloak user by the
 * JWT's "sub" claim. Created on the user's first authenticated request.
 */
@Entity
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The Keycloak user id (JWT "sub" claim). Never changes, so there's no setter.
    @Column(nullable = false, unique = true, updatable = false)
    private String keycloakId;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /*
     * The profile owns its survivor: the foreign key (survivor_id) lives in the
     * user_profile table. Null until the player creates their survivor.
     * unique = true: one survivor can't belong to two profiles.
     * Cascade: saving or deleting the profile saves or deletes its survivor.
     * LAZY: the survivor row is only loaded when used; getSurvivor().getId()
     * doesn't need it, because the id is already in survivor_id.
     * No separate survivorId field needed: this field IS the survivor_id column.
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "survivor_id", unique = true)
    private Survivor survivor;

    protected UserProfile() {
    }

    public UserProfile(String keycloakId, String displayName) {
        this.keycloakId = keycloakId;
        this.displayName = displayName;
        this.createdAt = Instant.now();
    }

    public boolean hasSurvivor() {
        return survivor != null;
    }

    // True if this profile owns the given survivor. Compares ids, since Hibernate
    // may hand back a different object instance for the same database row.
    public boolean owns(Survivor other) {
        return survivor != null && other != null
                && survivor.getId() != null
                && survivor.getId().equals(other.getId());
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Survivor getSurvivor() {
        return survivor;
    }

    public void setSurvivor(Survivor survivor) {
        //First, clear old survivor's link to user
        if (this.survivor != null) {
            this.survivor.setUser(null);
        }
        //Set survivor to incoming
        this.survivor = survivor;
        // Keep both sides in sync: the new survivor points back to this profile
        if (survivor != null) {
            survivor.setUser(this);
        }
    }
}
