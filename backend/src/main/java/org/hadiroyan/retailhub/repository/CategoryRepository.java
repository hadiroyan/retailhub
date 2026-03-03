package org.hadiroyan.retailhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}
