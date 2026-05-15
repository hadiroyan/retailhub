package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.Supplier;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SupplierRepository implements PanacheRepositoryBase<Supplier, UUID> {

    public List<Supplier> findByStore(UUID storeId, int page, int size) {
        return find("store.id = ?1", Sort.by("name").ascending(), storeId)
                .page(Page.of(page, size))
                .list();
    }

    public long countByStore(UUID storeId) {
        return count("store.id", storeId);
    }

    public Optional<Supplier> findByIdAndStore(UUID id, UUID storeId) {
        return find("id = ?1 AND store.id = ?2", id, storeId).firstResultOptional();
    }

    public boolean existsByNameAndStore(UUID storeId, String name) {
        return count("store.id = ?1 AND LOWER(name) = LOWER(?2)", storeId, name) > 0;
    }
}
