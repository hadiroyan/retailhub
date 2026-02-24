package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.StoreStatus;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreRepository implements PanacheRepositoryBase<Store, UUID> {

    public Optional<Store> findBySlug(String slug) {
        return find("slug", slug).firstResultOptional();
    }

    public boolean existsBySlug(String slug) {
        return count("slug", slug) > 0;
    }

    public boolean existsBySlugAndIdNot(String slug, UUID id) {
        return count("slug = ?1 AND id != ?2", slug, id) > 0;
    }

    // No filter (for SUPER_ADMIN)
    public List<Store> findAllPaged(int page, int size) {
        return findAll(Sort.by("createdAt").descending())
                .page(Page.of(page, size))
                .list();
    }

    // List store by owner (for OWNER)
    public List<Store> findByOwner(UUID ownerId, int page, int size) {
        return find("owner.id", Sort.by("createdAt").descending(), ownerId)
                .page(Page.of(page, size))
                .list();
    }

    // Active stores (for CUSTOMER)
    public List<Store> findAllActive(int page, int size) {
        return find("status", Sort.by("createdAt").descending(), "ACTIVE")
                .page(Page.of(page, size))
                .list();
    }

    public long countAll() {
        return count();
    }

    public long countByOwner(UUID ownerId) {
        return count("owner.id", ownerId);
    }

    public long countAllActive() {
        return count("status", StoreStatus.ACTIVE.name());
    }
}
