package org.hadiroyan.retailhub.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // UUID auto-generation
    public UUID id;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false)
    public String password;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(length = 20, nullable = false)
    public String provider = "LOCAL";

    @Column(name = "provider_id")
    public String providerId;

    @Column(name = "email_verified", nullable = false)
    public Boolean emailVerified = false;

    @Column(nullable = false)
    public Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<UserRole> userRoles = new HashSet<>();

    public User() {
    }

    /**
     * Constructor for LOCAL provider registration.
     */
    public User(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.provider = "LOCAL";
        this.emailVerified = false; // Not verified yet
        this.enabled = true; // But account is active
    }

    /**
     * Constructor for OAuth provider registration.
     */
    public User(String email, String fullName, String provider, String providerId) {
        this.email = email;
        this.password = ""; // No password for OAuth
        this.fullName = fullName;
        this.provider = provider;
        this.providerId = providerId;
        this.emailVerified = true; // Trust OAuth provider
        this.enabled = true;
    }

    public static User createLocal(String email, String password, String fullName) {
        return new User(email, password, fullName);
    }

    public static User createOAuth(String email, String fullName, String provider, String providerId) {
        return new User(email, fullName, provider, providerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof User))
            return false;
        User user = (User) obj;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", provider='" + provider + '\'' +
                ", emailVerified=" + emailVerified +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                '}';
    }

}
