package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.UserRole;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRoleRepository implements PanacheRepository<UserRole> {

    public List<UserRole> findByUserId(UUID userId) {
        return find("user.id", userId).list();
    }

    public List<UserRole> findByUserIdWithRoles(UUID userId) {
        return find("""
                SELECT DISTINCT ur FROM UserRole ur
                LEFT JOIN FETCH ur.role r
                LEFT JOIN FETCH r.privileges
                WHERE ur.user.id = ?1
                """,
                userId).list();
    }

    public Optional<UserRole> findByUserAndRole(UUID userId, Long roleId) {
        return find("""
                user.id = ?1 AND role.id = ?2
                """,
                userId,
                roleId).firstResultOptional();
    }

    public Optional<UserRole> findByUserRoleAndStore(UUID userId, Long roleId, UUID storeId) {
        return find("""
                user.id = ?1 AND role.id = ?2 AND storeId = ?3
                """,
                userId,
                roleId,
                storeId).firstResultOptional();
    }

    public List<UserRole> findGlobalRolesByUserId(UUID userId) {
        return find("user.id = ?1 AND storeId IS NULL", userId).list();
    }

    public List<UserRole> findStoreRolesByUserId(UUID userId, UUID storeId) {
        return find("""
                user.id = ?1 AND storeId = ?2
                """,
                userId,
                storeId).list();
    }

    public boolean userHasRoleInStore(UUID userId, String roleName, UUID storeId) {
        return count("""
                FROM UserRole ur
                JOIN ur.role r
                WHERE ur.user.id = ?1
                AND r.name = ?2
                AND ur.storeId = ?3
                """,
                userId, roleName, storeId) > 0;
    }

    public boolean userHasGlobalRole(UUID userId, String roleName) {
        return count("""
                FROM UserRole ur
                JOIN ur.role r
                WHERE ur.user.id = ?1
                AND r.name = ?2
                AND ur.storeId IS NULL
                """,
                userId, roleName) > 0;
    }

    public long deleteByUserId(UUID userId) {
        return delete("user.id", userId);
    }
}
