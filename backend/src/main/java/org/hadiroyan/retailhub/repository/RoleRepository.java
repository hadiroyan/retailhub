package org.hadiroyan.retailhub.repository;

import java.util.Optional;

import org.hadiroyan.retailhub.model.Role;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RoleRepository implements PanacheRepository<Role> {

    public Optional<Role> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    public Optional<Role> findByNameWithPrivileges(String name) {
        return find("""
                SELECT DISTINCT r
                FROM Role r
                LEFT JOIN FETCH r.privileges
                WHERE r.name = ?1
                """,
                name).firstResultOptional();
    }

    public boolean roleHasPrivilege(Long roleId, String privilegeName) {
        return count("""
                FROM Role r
                JOIN r.privileges p
                WHERE r.id = ?1 AND p.name = ?2
                """,
                roleId,
                privilegeName) > 0;
    }
}
