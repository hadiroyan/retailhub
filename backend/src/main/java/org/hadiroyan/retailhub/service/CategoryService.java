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
        LOG.debugf("action=CREATE_CATEGORY_START userId=%s storeId=%s name=%s",
                userId, storeId, request.name);
        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

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
        LOG.infof("action=CREATE_CATEGORY_SUCCESS userId=%s storeId=%s categoryId=%s slug=%s",
                userId, storeId, category.id, category.slug);

        // Single category — fetch data for one category only
        return buildCategoryResponse(category);
    }

    public PagedResponse<CategoryResponse> listCategories(UUID storeId, int page, int size) {
        LOG.debugf("action=LIST_CATEGORIES_START storeId=%s page=%d size=%d",
                storeId, page, size);
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

        LOG.infof("action=LIST_CATEGORIES_SUCCESS storeId=%s total=%d page=%d size=%d",
                storeId, total, page, size);
        return new PagedResponse<>(content, page, size, total);
    }

    public CategoryResponse getCategoryBySlug(UUID storeId, String slug) {
        LOG.debugf("action=GET_CATEGORY_BY_SLUG_START storeId=%s slug=%s",
                storeId, slug);
        findStoreOrThrow(storeId);

        Category category = categoryRepository.findByStoreAndSlug(storeId, slug)
                .orElseThrow(() -> {
                    LOG.warnf("action=CATEGORY_NOT_FOUND storeId=%s slug=%s",
                            storeId, slug);
                    return new NotFoundException("Category not found");
                });

        LOG.infof("action=GET_CATEGORY_BY_SLUG_SUCCESS storeId=%s categoryId=%s slug=%s",
                storeId, category.id, slug);
        return buildCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID storeId, UUID categoryId,
            UUID userId, UpdateCategoryRequest request) {

        LOG.debugf("action=UPDATE_CATEGORY_START userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);
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

        category.name = request.name;
        category.slug = generateSlugForUpdate(request.name, storeId, categoryId);
        category.description = request.description;
        category.imageUrl = request.imageUrl;
        category.parent = parent;

        LOG.infof("action=UPDATE_CATEGORY_SUCCESS userId=%s storeId=%s categoryId=%s slug=%s",
                userId, storeId, category.id, category.slug);
        return buildCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID storeId, UUID categoryId, UUID userId) {
        LOG.debugf("action=DELETE_CATEGORY_START userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Category category = findCategoryOrThrow(categoryId);
        validateSameStore(category, storeId);

        categoryRepository.delete(category);
        LOG.infof("action=DELETE_CATEGORY_SUCCESS userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);
    }

    // Helper -------------------------------------------------
    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> {
                    LOG.warnf("action=CATEGORY_NOT_FOUND categoryId=%s", categoryId);
                    return new NotFoundException("Category not found");
                });
    }

    private void validateSameStore(Category category, UUID storeId) {
        if (!category.store.id.equals(storeId)) {
            LOG.warnf("action=CATEGORY_STORE_MISMATCH categoryId=%s categoryStoreId=%s requestedStoreId=%s",
                    category.id, category.store.id, storeId);
            throw new BadRequestException("Category does not belong to this store");
        }
    }

    private void validateRootCategory(Category category) {
        if (category.parent != null) {
            LOG.warnf("action=INVALID_PARENT_CATEGORY categoryId=%s parentId=%s",
                    category.id, category.parent.id);
            throw new BadRequestException("Max 2 levels allowed");
        }
    }

    private void validateNotSelf(Category category, Category parent) {
        if (category.id.equals(parent.id)) {
            LOG.warnf("action=CATEGORY_SELF_PARENT categoryId=%s", category.id);
            throw new BadRequestException("Category cannot be its own parent");
        }
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);

        if (!canWrite) {
            LOG.warnf("action=CATEGORY_WRITE_DENIED userId=%s storeId=%s",
                    userId, storeId);
            throw new ForbiddenException("No permission");
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
