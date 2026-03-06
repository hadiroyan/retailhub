package org.hadiroyan.retailhub.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.CategoryResponse;
import org.hadiroyan.retailhub.model.Category;
import org.hadiroyan.retailhub.repository.CategoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CategoryMapper {

    @Inject
    CategoryRepository categoryRepository;

    public CategoryResponse toResponse(
            Category category,
            Map<UUID, Long> productCounts,
            Map<UUID, List<Category>> childrenMap) {

        CategoryResponse response = new CategoryResponse();

        response.id = category.id;
        response.name = category.name;
        response.slug = category.slug;
        response.description = category.description;
        response.imageUrl = category.imageUrl;
        response.createdAt = category.createdAt;
        response.updatedAt = category.updatedAt;

        response.productCount = productCounts.getOrDefault(category.id, 0L);

        if (category.parent != null) {
            CategoryResponse.ParentInfo parentInfo = new CategoryResponse.ParentInfo();
            parentInfo.id = category.parent.id;
            parentInfo.name = category.parent.name;
            parentInfo.slug = category.parent.slug;
            response.parent = parentInfo;
        }

        if (category.parent == null) {
            List<Category> children = childrenMap.getOrDefault(category.id, List.of());
            response.children = children.stream()
                    .map(child -> {
                        CategoryResponse.ChildInfo childInfo = new CategoryResponse.ChildInfo();
                        childInfo.id = child.id;
                        childInfo.name = child.name;
                        childInfo.slug = child.slug;
                        childInfo.productCount = productCounts.getOrDefault(child.id, 0L);
                        return childInfo;
                    })
                    .toList();
        }

        return response;
    }
}
