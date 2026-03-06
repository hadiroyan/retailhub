package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateCategoryRequest;
import org.hadiroyan.retailhub.dto.response.CategoryResponse;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.CategoryMapper;
import org.hadiroyan.retailhub.model.Category;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.StoreStatus;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.repository.CategoryRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.hadiroyan.retailhub.utils.SlugUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.security.ForbiddenException;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks
    CategoryService categoryService;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    StoreRepository storeRepository;

    @Mock
    UserRoleRepository userRoleRepository;

    @Mock
    CategoryMapper categoryMapper;

    @Mock
    SlugUtil slugUtil;

    UUID ownerId;
    UUID storeId;

    User mockUser;
    Store mockStore;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.id = ownerId;
        mockUser.email = "owner@mail.com";

        mockStore = new Store();
        mockStore.id = storeId;
        mockStore.owner = mockUser;
        mockStore.name = "Test Store";
        mockStore.slug = "test-store";
        mockStore.status = StoreStatus.ACTIVE.name();
    }

    @Test
    void should_success_create_category() {

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.name = "Electronics";

        CategoryResponse response = new CategoryResponse();
        response.name = "Electronics";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(mockStore));

        when(userRoleRepository.userHasAnyRoleInStore(
                mockUser.id,
                Set.of("OWNER", "ADMIN", "MANAGER"),
                storeId))
                .thenReturn(true);

        when(categoryRepository.existsByStoreAndSlug(eq(storeId), anyString()))
                .thenReturn(false);

        when(categoryRepository.findChildrenByParentIds(anyList()))
                .thenReturn(Map.of());

        when(categoryRepository.countProductsByCategoryIds(anyList()))
                .thenReturn(Map.of());

        when(categoryMapper.toResponse(any(), anyMap(), anyMap()))
                .thenReturn(response);

        // simulate DB generated id
        doAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.id = UUID.randomUUID();
            return null;
        }).when(categoryRepository).persist(any(Category.class));

        CategoryResponse result = categoryService.createCategory(storeId, mockUser.id, request);

        assertNotNull(result);
        assertEquals("Electronics", result.name);

        verify(categoryRepository).persist(any(Category.class));
    }

    @Test
    void should_throw_not_found_when_store_missing() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.name = "Electronics";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> categoryService.createCategory(storeId, mockUser.id, request));
    }

    @Test
    void should_throw_forbidden_when_user_no_permission() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.name = "Electronics";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(mockStore));

        when(userRoleRepository.userHasAnyRoleInStore(
                mockUser.id,
                Set.of("OWNER", "ADMIN", "MANAGER"),
                storeId))
                .thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> categoryService.createCategory(storeId, mockUser.id, request));
    }
}
