package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;

import org.hadiroyan.retailhub.model.Privilege;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrivilegeRepository implements PanacheRepository<Privilege> {

    public Optional<Privilege> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public List<Privilege> findByResource(String resource) {
        return find("resource", resource).list();
    }

    public List<Privilege> findByNames(List<String> names) {
        return find("name IN ?1", names).list();
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }
}
