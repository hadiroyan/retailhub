package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;

import org.hadiroyan.retailhub.model.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByEmailWithRoles(String email) {
        return find("""
                SELECT DISTINCT u FROM User u
                LEFT JOIN FETCH u.userRoles ur
                LEFT JOIN FETCH ur.role
                WHERE u.email = ?1
                """,
                email).firstResultOptional();
    }

    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return find("""
                provider = ?1 AND providerId = ?2
                """,
                provider,
                providerId).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public List<User> findEnabledUsers() {
        return find("enabled", true).list();
    }

    public List<User> findByRoleName(String roleName) {
        return find("""
                SELECT DISTINCT u FROM User u
                JOIN u.userRoles ur
                JOIN ur.role r
                WHERE r.name = ?1
                """,
                roleName).list();
    }

    public long countByRole(String roleName) {
        return count("""
                FROM User u
                JOIN u.userRoles ur
                JOIN ur.role r
                WHERE r.name = ?1
                """,
                roleName);
    }

}
