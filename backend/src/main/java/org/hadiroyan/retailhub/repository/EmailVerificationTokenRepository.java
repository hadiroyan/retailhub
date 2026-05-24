package org.hadiroyan.retailhub.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.EmailVerificationToken;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailVerificationTokenRepository implements PanacheRepositoryBase<EmailVerificationToken, UUID>{

    public Optional<EmailVerificationToken> findLatestActiveByUserId(UUID userId) {
        return find("""
                FROM EmailVerificationToken t
                WHERE t.user.id = ?1
                  AND t.used = false
                  AND t.expiresAt > ?2
                ORDER BY t.createdAt DESC
                """,
                userId, LocalDateTime.now())
                .firstResultOptional();
    }

    public long countRecentByUserId(UUID userId, LocalDateTime since) {
        return count("user.id = ?1 AND createdAt >= ?2", userId, since);
    }

    public void invalidateAllByUserId(UUID userId) {
        update("used = true WHERE user.id = ?1 AND used = false", userId);
    }
}
