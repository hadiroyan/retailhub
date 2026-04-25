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

    // Global — All products from all ACTIVE stores
    // Filter: name, storeId, categoryId, sortByPrice
    public List<Product> findAllActive(String name, UUID storeId, UUID categoryId,
            String sortByPrice, int page, int size) {

        StringBuilder query = new StringBuilder("""
                SELECT p FROM Product p
                JOIN p.store s
                WHERE s.status = 'ACTIVE'
                AND p.status = 'ACTIVE'
                """);

        Parameters params = new Parameters();

        if (name != null && !name.isBlank()) {
            query.append("AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) ");
            params.and("name", name);
        }

        if (storeId != null) {
            query.append("AND s.id = :storeId ");
            params.and("storeId", storeId);
        }

        if (categoryId != null) {
            query.append("AND p.category.id = :categoryId ");
            params.and("categoryId", categoryId);
        }

        if ("asc".equalsIgnoreCase(sortByPrice)) {
            query.append("ORDER BY p.price ASC");
        } else if ("desc".equalsIgnoreCase(sortByPrice)) {
            query.append("ORDER BY p.price DESC");
        } else {
            query.append("ORDER BY p.createdAt DESC");
        }

        return find(query.toString(), params)
                .page(Page.of(page, size))
                .list();
    }

    public long countAllActive(String name, UUID storeId, UUID categoryId) {
        StringBuilder query = new StringBuilder("""
                SELECT COUNT(p) FROM Product p
                JOIN p.store s
                WHERE s.status = 'ACTIVE'
                AND p.status = 'ACTIVE'
                """);

        Parameters params = new Parameters();

        if (name != null && !name.isBlank()) {
            query.append("AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) ");
            params.and("name", name);
        }

        if (storeId != null) {
            query.append("AND s.id = :storeId ");
            params.and("storeId", storeId);
        }

        if (categoryId != null) {
            query.append("AND p.category.id = :categoryId ");
            params.and("categoryId", categoryId);
        }

        return count(query.toString(), params);
    }
}
