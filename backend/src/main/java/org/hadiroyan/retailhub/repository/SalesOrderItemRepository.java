package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.model.SalesOrderItem;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SalesOrderItemRepository implements PanacheRepositoryBase<SalesOrderItem, UUID> {

    public List<SalesOrderItem> findByOrderId(UUID orderId) {
        return find("salesOrder.id", orderId).list();
    }

    public long countByOrderId(UUID orderId) {
        return count("salesOrder.id", orderId);
    }
}