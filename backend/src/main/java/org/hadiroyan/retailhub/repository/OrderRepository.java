package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.model.SalesOrder;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<SalesOrder, UUID> {

    // Customer — list of orders for a customer
    public List<SalesOrder> findByCustomer(UUID customerId, int page, int size) {
        return find("customer.id = ?1",
                Sort.by("createdAt").descending(), customerId)
                .page(Page.of(page, size))
                .list();
    }

    public long countByCustomer(UUID customerId) {
        return count("customer.id", customerId);
    }

    // Store — list of orders received by the store
    public List<SalesOrder> findByStore(UUID storeId, String status, int page, int size) {
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
}