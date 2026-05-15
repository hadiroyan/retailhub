package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.PurchaseOrder;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PurchaseOrderRepository implements PanacheRepositoryBase<PurchaseOrder, UUID> {

    public List<PurchaseOrder> findByStore(UUID storeId, String status, int page, int size) {
        if (status != null && !status.isBlank()) {
            return find("store.id = ?1 AND status = ?2",
                    Sort.by("createdAt").descending(), storeId, status)
                    .page(Page.of(page, size))
                    .list();
        }
        return find("store.id = ?1",
                Sort.by("createdAt").descending(), storeId)
                .page(Page.of(page, size))
                .list();
    }

    public long countByStore(UUID storeId, String status) {
        if (status != null && !status.isBlank()) {
            return count("store.id = ?1 AND status = ?2", storeId, status);
        }
        return count("store.id", storeId);
    }

    public Optional<PurchaseOrder> findByIdAndStore(UUID id, UUID storeId) {
        return find("id = ?1 AND store.id = ?2", id, storeId).firstResultOptional();
    }

}
