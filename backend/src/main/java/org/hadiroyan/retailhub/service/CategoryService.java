package org.hadiroyan.retailhub.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateCategoryRequest;
import org.hadiroyan.retailhub.dto.request.UpdateCategoryRequest;
import org.hadiroyan.retailhub.dto.response.CategoryResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.CategoryMapper;
import org.hadiroyan.retailhub.model.Category;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.repository.CategoryRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.hadiroyan.retailhub.utils.SlugUtil;
import org.jboss.logging.Logger;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoryService {

    private static final Logger LOG = Logger.getLogger(CategoryService.class);

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(UUID storeId, UUID userId, CreateCategoryRequest request) {
        LOG.infof("Create category request for category: %s ", request.name);
        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        LOG.infof("Validate create category: %s", request.name);
        Category parent = null;
        if (request.parentId != null) {
            parent = findCategoryOrThrow(request.parentId);
            validateSameStore(parent, storeId);
            validateRootCategory(parent);
            LOG.infof("success validate category: %s", request.name);
        }

        String slug = generateSlug(request.name, storeId);
        LOG.infof("Success create slug for category %s", request.name);

        Category category = new Category();
        category.store = store;
        category.name = request.name;
        category.slug = slug;
        category.description = request.description;
        category.imageUrl = request.imageUrl;
        category.parent = parent;

        categoryRepository.persist(category);
        LOG.infof("Success persist category %s", request.name);

        // Single category — fetch data for one category only
        return buildCategoryResponse(category);
    }

    public PagedResponse<CategoryResponse> listCategories(UUID storeId, int page, int size) {
        LOG.infof("Get list category for store id: %s", storeId);
        LOG.info("Validate store and category");
        findStoreOrThrow(storeId);

        List<Category> roots = categoryRepository.findRootsByStore(storeId);
        long total = categoryRepository.countByStore(storeId);

        List<UUID> rootIds = roots.stream().map(c -> c.id).toList();

        Map<UUID, List<Category>> childrenMap = categoryRepository.findChildrenByParentIds(rootIds);

        List<UUID> allIds = collectAllIds(rootIds, childrenMap);

        Map<UUID, Long> productCounts = categoryRepository.countProductsByCategoryIds(allIds);
        List<CategoryResponse> content = roots.stream()
                .map(c -> categoryMapper.toResponse(c, productCounts, childrenMap))
                .toList();

        LOG.infof("Success get list category page %d and size %d", page, size);
        return new PagedResponse<>(content, page, size, total);
    }

    public CategoryResponse getCategoryBySlug(UUID storeId, String slug) {
        LOG.infof("Get category by slug: %s", slug);
        findStoreOrThrow(storeId);

        Category category = categoryRepository.findByStoreAndSlug(storeId, slug)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        return buildCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID storeId, UUID categoryId,
            UUID userId, UpdateCategoryRequest request) {

        LOG.infof("Update cateogry: %s with user ID: %s", String.valueOf(categoryId), String.valueOf(userId));
        LOG.infof("validate for update category by user ID: %s", String.valueOf(userId));
        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Category category = findCategoryOrThrow(categoryId);
        validateSameStore(category, storeId);

        Category parent = null;
        if (request.parentId != null) {
            parent = findCategoryOrThrow(request.parentId);
            validateSameStore(parent, storeId);
            validateRootCategory(parent);
            validateNotSelf(category, parent);
        }
        LOG.infof("Updating category ID: %s by user ID: %s", String.valueOf(categoryId), String.valueOf(userId));

        category.name = request.name;
        category.slug = generateSlugForUpdate(request.name, storeId, categoryId);
        category.description = request.description;
        category.imageUrl = request.imageUrl;
        category.parent = parent;

        LOG.infof("Success persist update category %s", request.name);
        return buildCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID storeId, UUID categoryId, UUID userId) {
        LOG.infof("validate for delete category ID: %s", String.valueOf(categoryId));
        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Category category = findCategoryOrThrow(categoryId);
        validateSameStore(category, storeId);

        categoryRepository.delete(category);
        LOG.infof("Success delete category ID: %s by user ID: %s", String.valueOf(categoryId), String.valueOf(userId));
    }

    // Helper -------------------------------------------------
    private Store findStoreOrThrow(UUID storeId) {
        LOG.infof("find store with id: %s", storeId);
        return storeRepository.findByIdOptional(storeId).orElseThrow(() -> new NotFoundException("Store not found!"));
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        LOG.infof("find category with id: %s", categoryId);
        return categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found!"));
    }

    private void validateSameStore(Category category, UUID storeId) {
        LOG.infof("validate same store for category (slug): %s", category.slug);
        if (!category.store.id.equals(storeId)) {
            throw new BadRequestException("Category does not belong to this store");
        }
    }

    private void validateRootCategory(Category category) {
        LOG.infof("validate root for category (slug): %s", category.slug);
        if (category.parent != null) {
            throw new BadRequestException("Cannot assign a child category as parent — maximum 2 levels allowed");
        }
    }

    private void validateNotSelf(Category category, Category parent) {
        LOG.infof("validate category not itself for category (slug): %s", category.slug);
        if (category.id.equals(parent.id)) {
            throw new BadRequestException("Category cannot be its own parent");
        }
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);
        LOG.infof("check permission for user id: %s [is can write: %b]", String.valueOf(userId), canWrite);
        if (!canWrite) {
            throw new ForbiddenException("You don't have permission to manage this store's categories");
        }
    }

    private CategoryResponse buildCategoryResponse(Category category) {
        LOG.infof("build category response for category (slug): %s", category.slug);
        List<UUID> ids = List.of(category.id);

        Map<UUID, List<Category>> childrenMap = categoryRepository.findChildrenByParentIds(ids);

        // Collect all IDs, including children's IDs, to calculate the number of
        // products.
        List<UUID> allIds = collectAllIds(ids, childrenMap);
        Map<UUID, Long> productCounts = categoryRepository.countProductsByCategoryIds(allIds);

        LOG.infof("Finished build category response for category (slug)", category.slug);
        return categoryMapper.toResponse(category, productCounts, childrenMap);
    }

    private List<UUID> collectAllIds(List<UUID> rootIds, Map<UUID, List<Category>> childrenMap) {
        LOG.infof("collect all category id for children", childrenMap);
        List<UUID> allIds = new java.util.ArrayList<>(rootIds);
        childrenMap.values().forEach(children -> children.forEach(child -> allIds.add(child.id)));
        return allIds;
    }

    private String generateSlug(String name, UUID storeId) {
        LOG.infof("generate slug for %s in store id %d", name, String.valueOf(storeId));
        String baseSlug = SlugUtil.toSlug(name);
        String candidate = baseSlug;
        int counter = 2;
        while (categoryRepository.existsByStoreAndSlug(storeId, candidate)) {
            candidate = baseSlug + "-" + counter++;
        }
        LOG.infof("Done creating the slug: %s", candidate);
        return candidate;
    }

    private String generateSlugForUpdate(String name, UUID storeId, UUID excludeId) {
        LOG.infof("generate slug for update %s in store id %d", name, String.valueOf(storeId));
        String baseSlug = SlugUtil.toSlug(name);
        String candidate = baseSlug;
        int counter = 2;
        while (categoryRepository.existsByStoreAndSlugAndIdNot(storeId, candidate, excludeId)) {
            candidate = baseSlug + "-" + counter++;
        }
        LOG.infof("Done updating the slug to: %s", candidate);
        return candidate;
    }
}
