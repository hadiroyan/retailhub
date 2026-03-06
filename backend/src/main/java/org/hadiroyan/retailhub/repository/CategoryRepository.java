package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hadiroyan.retailhub.model.Category;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoryRepository implements PanacheRepositoryBase<Category, UUID> {

    public Optional<Category> findByStoreAndSlug(UUID storeId, String slug) {
        return find("store.id = ?1 AND slug = ?2", storeId, slug).firstResultOptional();
    }

    public boolean existsByStoreAndSlug(UUID storeId, String slug) {
        return count("store.id = ?1 AND slug = ?2", storeId, slug) > 0;
    }

    public boolean existsByStoreAndSlugAndIdNot(UUID storeId, String slug, UUID id) {
        return count("store.id = ?1 AND slug = ?2 AND id != ?3", storeId, slug, id) > 0;
    }

    public List<Category> findRootsByStore(UUID storeId) {
        return find("store.id = ?1 AND parent IS NULL", Sort.by("name").ascending(), storeId).list();
    }

    public List<Category> findChildrenByParent(UUID parentId) {
        return find("parent.id = ?1", Sort.by("name").ascending(), parentId).list();
    }

    public List<Category> findAllByStore(UUID storeId, int page, int size) {
        return find("store.id = ?1", Sort.by("name").ascending(), storeId).page(page, size).list();
    }

    public long countByStore(UUID storeId) {
        return count("store.id = ?1", storeId);
    }

    public long countProductsByCategory(UUID categoryId) {
        return getEntityManager()
                .createQuery("""
                        SELECT COUNT (p) FROM Product p
                        WHERE p.category.id = :categoryId
                        """, Long.class)
                .setParameter("categoryId", categoryId)
                .getSingleResult();
    }

    public Map<UUID, Long> countProductsByCategoryIds(List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = getEntityManager()
                .createQuery("""
                        SELECT p.category.id, COUNT(p)
                        FROM Product p
                        WHERE p.category.id IN :categoryIds
                        GROUP BY p.category.id
                        """, Object[].class)
                .setParameter("categoryIds", categoryIds)
                .getResultList();

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    public Map<UUID, List<Category>> findChildrenByParentIds(List<UUID> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Map.of();
        }

        List<Category> allChildren = find("parent.id IN ?1",
                Sort.by("name").ascending(), parentIds)
                .list();

        return allChildren.stream()
                .collect(Collectors.groupingBy(child -> child.parent.id));
    }
}
