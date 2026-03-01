package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateStoreRequest;
import org.hadiroyan.retailhub.dto.request.UpdateStoreStatusRequest;
import org.hadiroyan.retailhub.dto.response.StoreResponse;
import org.hadiroyan.retailhub.exception.ForbiddenException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.StoreMapper;
import org.hadiroyan.retailhub.model.Role;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.StoreStatus;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.model.UserRole;
import org.hadiroyan.retailhub.repository.RoleRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.hadiroyan.retailhub.utils.SlugUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    StoreRepository storeRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserRoleRepository userRoleRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    SlugUtil slugUtil;

    @Mock
    StoreMapper storeMapper = new StoreMapper();

    @InjectMocks
    StoreService storeService;

    private User mockUser;
    private Store mockStore;
    private StoreResponse mockStoreResponse;
    private UUID storeId;
    private UUID ownerId;

    @BeforeEach
    void setup() {
        ownerId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.id = ownerId;
        mockUser.email = "owner.mail@example.com";

        mockStore = new Store();
        mockStore.id = storeId;
        mockStore.owner = mockUser;
        mockStore.name = "Old Store";
        mockStore.slug = "old-store";
        mockStore.status = StoreStatus.ACTIVE.name();

        mockStoreResponse = new StoreResponse();
        mockStoreResponse.id = storeId;
        mockStoreResponse.name = "Old Store";
        mockStoreResponse.slug = "old-store";
        mockStoreResponse.status = StoreStatus.ACTIVE.name();
    }

    @Test
    void should_create_store_when_owner_exists() {

        CreateStoreRequest request = new CreateStoreRequest();
        request.name = "Old Store";

        Role ownerRole = new Role();
        ownerRole.name = "OWNER";

        when(userRepository.findByEmail("owner.mail@example.com"))
                .thenReturn(Optional.of(mockUser));
        when(slugUtil.generateUniqueSlug("Old Store"))
                .thenReturn("old-store");
        when(roleRepository.findByName("OWNER"))
                .thenReturn(Optional.of(ownerRole));
        when(storeMapper.toResponse(any(Store.class)))
                .thenReturn(mockStoreResponse);

        StoreResponse result = storeService.createStore("owner.mail@example.com", request);

        assertNotNull(result);
        assertEquals("Old Store", result.name);
        assertEquals("old-store", result.slug);

        verify(storeRepository).persist(any(Store.class));
        verify(userRoleRepository).persist(any(UserRole.class));
        verify(storeMapper).toResponse(any(Store.class));

    }

    @Test
    void should_throw_exception_when_user_not_found() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.name = "Some Store";

        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> storeService.createStore("notfound@example.com", request));
    }

    // Get store by slug
    @Test
    void should_return_store_when_active() {
        when(storeRepository.findBySlug("old-store"))
                .thenReturn(Optional.of(mockStore));
        when(storeMapper.toResponse(mockStore))
                .thenReturn(mockStoreResponse);

        StoreResponse result = storeService.getStoreBySlug("old-store",
                null,
                Set.of());

        assertNotNull(result);
    }

    @Test
    void should_throw_NotFoundException_when_store_not_found() {
        when(storeRepository.findBySlug("not-exists")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> storeService.getStoreBySlug("not-exists", null, Set.of("ANONYMOUS")));
    }

    // Delete Store
    @Test
    void should_delete_store_when_role_is_the_owner() {
        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(mockStore));

        storeService.deleteStore(storeId,
                ownerId,
                Set.of("OWNER"));

        verify(storeRepository).delete(mockStore);
    }

    @Test
    void should_throw_ForbiddenException_when_caller_is_not_the_owner() {
        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(mockStore));

        UUID anotherUser = UUID.randomUUID();

        assertThrows(ForbiddenException.class, () -> storeService.deleteStore(storeId,
                anotherUser,
                Set.of("OWNER")));
    }

    // Update Status
    @Test
    void should_allow_SUPER_ADMIN_to_SUSPEND_store() {
        UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
        request.status = "SUSPEND";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(mockStore));
        when(storeMapper.toResponse(mockStore))
                .thenReturn(mockStoreResponse);

        StoreResponse result = storeService.updateStatus(storeId,
                ownerId,
                Set.of("SUPER_ADMIN"),
                request);

        assertNotNull(result);
        assertEquals(StoreStatus.SUSPEND.name(), mockStore.status);
    }

    @Test
    void should_throw_ForbiddenException_when_OWNER_tries_to_SUSPEND_store() {
        UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
        request.status = "SUSPEND";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(mockStore));

        assertThrows(ForbiddenException.class, () -> storeService.updateStatus(storeId,
                ownerId,
                Set.of("OWNER"),
                request), "Only SUPER_ADMIN can suspend a store");
    }
}
