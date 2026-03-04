package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.Product;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, UUID> {

    public Optional<Product> findByStoreAndSku(UUID storeId, String sku) {
        return find("store.id = ?1 AND sku = ?2", storeId, sku).firstResultOptional();
    }

    public boolean existsByStoreAndSku(UUID storeId, String sku) {
        return count("store.id = ?1 AND sku = ?2", storeId, sku) > 0;
    }

    public boolean existsByStoreAndSkuAndIdNot(UUID storeId, String sku, UUID id) {
        return count("store.id = ?1 AND sku = ?2 AND id != ?3", storeId, sku, id) > 0;
    }

    public List<Product> findByStore(UUID storeId, String name, UUID categoryId,
            String sortByPrice, int page, int size) {

        String jpql = buildFilterQuery(storeId, name, categoryId);
        Sort sort = buildSort(sortByPrice);

        return find(jpql, sort, buildParams(storeId, name, categoryId))
                .page(Page.of(page, size))
                .list();
    }

    public long countByStore(UUID storeId, String name, UUID categoryId) {
        String jpql = buildFilterQuery(storeId, name, categoryId);
        return count(jpql, buildParams(storeId, name, categoryId));
    }

    public long countAllByStore(UUID storeId) {
        return count("store.id", storeId);
    }

    private String buildFilterQuery(UUID storeId, String name, UUID categoryId) {
        StringBuilder query = new StringBuilder("store.id = :storeId");

        if (name != null && !name.isBlank()) {
            query.append(" AND LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))");
        }

        if (categoryId != null) {
            query.append(" AND category.id = :categoryId");
        }

        return query.toString();
    }

    private Parameters buildParams(UUID storeId, String name, UUID categoryId) {
        Parameters params = Parameters.with("storeId", storeId);

        if (name != null && !name.isBlank()) {
            params.and("name", name);
        }

        if (categoryId != null) {
            params.and("categoryId", categoryId);
        }

        return params;
    }

    private Sort buildSort(String sortByPrice) {
        if ("asc".equalsIgnoreCase(sortByPrice)) {
            return Sort.by("price").ascending();
        } else if ("desc".equalsIgnoreCase(sortByPrice)) {
            return Sort.by("price").descending();
        }
        return Sort.by("name").ascending(); // default
    }
}
