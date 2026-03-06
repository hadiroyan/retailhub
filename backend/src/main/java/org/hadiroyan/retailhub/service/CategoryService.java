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

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoryService {

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

        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Category parent = null;
        if (request.parentId != null) {
            parent = findCategoryOrThrow(request.parentId);
            validateSameStore(parent, storeId);
            validateRootCategory(parent);
        }

        String slug = generateSlug(request.name, storeId);

        Category category = new Category();
        category.store = store;
        category.name = request.name;
        category.slug = slug;
        category.description = request.description;
        category.imageUrl = request.imageUrl;
        category.parent = parent;

        categoryRepository.persist(category);

        // Single category — fetch data for one category only
        return buildCategoryResponse(category);
    }

    public PagedResponse<CategoryResponse> listCategories(UUID storeId, int page, int size) {
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

        return new PagedResponse<>(content, page, size, total);
    }

    public CategoryResponse getCategoryBySlug(UUID storeId, String slug) {
        findStoreOrThrow(storeId);

        Category category = categoryRepository.findByStoreAndSlug(storeId, slug)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        return buildCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID storeId, UUID categoryId,
            UUID userId, UpdateCategoryRequest request) {

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

        return buildCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID storeId, UUID categoryId, UUID userId) {
        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Category category = findCategoryOrThrow(categoryId);
        validateSameStore(category, storeId);

        categoryRepository.delete(category);
    }

    // Helper -------------------------------------------------
    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId).orElseThrow(() -> new NotFoundException("Store not found!"));
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found!"));
    }

    private void validateSameStore(Category category, UUID storeId) {
        if (!category.store.id.equals(storeId)) {
            throw new BadRequestException("Category does not belong to this store");
        }
    }

    private void validateRootCategory(Category category) {
        if (category.parent != null) {
            throw new BadRequestException("Cannot assign a child category as parent — maximum 2 levels allowed");
        }
    }

    private void validateNotSelf(Category category, Category parent) {
        if (category.id.equals(parent.id)) {
            throw new BadRequestException("Category cannot be its own parent");
        }
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);
        if (!canWrite) {
            throw new ForbiddenException("You don't have permission to manage this store's categories");
        }
    }

    private CategoryResponse buildCategoryResponse(Category category) {
        List<UUID> ids = List.of(category.id);

        Map<UUID, List<Category>> childrenMap = categoryRepository.findChildrenByParentIds(ids);

        // Collect all IDs, including children's IDs, to calculate the number of
        // products.
        List<UUID> allIds = collectAllIds(ids, childrenMap);
        Map<UUID, Long> productCounts = categoryRepository.countProductsByCategoryIds(allIds);

        return categoryMapper.toResponse(category, productCounts, childrenMap);
    }

    private List<UUID> collectAllIds(List<UUID> rootIds, Map<UUID, List<Category>> childrenMap) {
        List<UUID> allIds = new java.util.ArrayList<>(rootIds);
        childrenMap.values().forEach(children -> children.forEach(child -> allIds.add(child.id)));
        return allIds;
    }

    private String generateSlug(String name, UUID storeId) {
        String baseSlug = SlugUtil.toSlug(name);
        String candidate = baseSlug;
        int counter = 2;
        while (categoryRepository.existsByStoreAndSlug(storeId, candidate)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }

    private String generateSlugForUpdate(String name, UUID storeId, UUID excludeId) {
        String baseSlug = SlugUtil.toSlug(name);
        String candidate = baseSlug;
        int counter = 2;
        while (categoryRepository.existsByStoreAndSlugAndIdNot(storeId, candidate, excludeId)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }
}
