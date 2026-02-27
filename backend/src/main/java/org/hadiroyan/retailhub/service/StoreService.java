package org.hadiroyan.retailhub.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateStoreRequest;
import org.hadiroyan.retailhub.dto.request.UpdateStoreRequest;
import org.hadiroyan.retailhub.dto.request.UpdateStoreStatusRequest;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StoreService {

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    StoreMapper storeMapper;

    @Inject
    SlugUtil slugUtil;

    @Transactional
    public StoreResponse createStore(String email, CreateStoreRequest request) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        Store store = new Store();
        store.owner = owner;
        store.name = request.name;
        store.slug = slugUtil.generateUniqueSlug(request.name);
        store.description = request.description;
        store.address = request.address;
        store.phone = request.phone;
        store.email = request.email;
        store.status = StoreStatus.ACTIVE.name();

        storeRepository.persist(store);

        assignOwnerRole(owner, store);

        return storeMapper.toResponse(store);
    }

    private void assignOwnerRole(User owner, Store store) {
        Role role = roleRepository.findByName("OWNER")
                .orElseThrow(() -> new NotFoundException("Role is not found [OWNER]"));

        UserRole userRole = new UserRole();
        userRole.user = owner;
        userRole.storeId = store.id;
        userRole.role = role;

        userRoleRepository.persist(userRole);
    }

    public PagedResponse<StoreResponse> listsStores(UUID ownerId, Set<String> roles, int page, int size) {
        List<Store> stores;
        long total;

        if (roles.contains("SUPER_ADMIN")) {
            stores = storeRepository.findAllPaged(page, size);
            total = storeRepository.countAll();
        } else if (roles.contains("OWNER")) {
            stores = storeRepository.findByOwner(ownerId, page, size);
            total = storeRepository.countByOwner(ownerId);
        } else {
            stores = storeRepository.findAllActive(page, size);
            total = storeRepository.countAllActive();
        }

        List<StoreResponse> data = stores.stream()
                .map(storeMapper::toResponse)
                .toList();

        return new PagedResponse<>(data, page, size, total);
    }

    public StoreResponse getStoreBySlug(String slug, UUID userId, Set<String> roles) {
        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        boolean isActive = StoreStatus.ACTIVE.name().equals(store.status);
        boolean isSuperAdmin = roles.contains("SUPER_ADMIN");
        boolean isOwner = store.owner != null && store.owner.id.equals(userId);

        if (!isActive && !isSuperAdmin && !isOwner) {
            throw new NotFoundException("Store not found");
        }

        return storeMapper.toResponse(store);
    }

    @Transactional
    public StoreResponse updateStore(UUID storeId, UUID userId, Set<String> roles,
            UpdateStoreRequest request) {
        Store store = storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        assertCanModify(store, userId, roles);

        store.name = request.name;
        store.slug = slugUtil.generateUniqueSlugForUpdate(request.name, storeId);
        store.description = request.description;
        store.address = request.address;
        store.phone = request.phone;
        store.email = request.email;

        return storeMapper.toResponse(store);
    }

    @Transactional
    public void deleteStore(UUID storeId, UUID userId, Set<String> roles) {
        Store store = storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        assertCanModify(store, userId, roles);

        storeRepository.delete(store);
    }

    @Transactional
    public StoreResponse updateStatus(UUID storeId, UUID userId, Set<String> roles, UpdateStoreStatusRequest request) {
        Store store = storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));

        assertCanModify(store, userId, roles);
        StoreStatus newStatus = StoreStatus.valueOf(request.status);

        // OWNER cannot set the SUSPEND status
        // This is the platform's right (SUPER_ADMIN)
        boolean isSuperAdmin = roles.contains("SUPER_ADMIN");
        if (!isSuperAdmin && newStatus == StoreStatus.SUSPEND) {
            throw new ForbiddenException("Only SUPER_ADMIN can suspend a store");
        }

        store.status = newStatus.name();
        return storeMapper.toResponse(store);
    }

    private void assertCanModify(Store store, UUID userId, Set<String> roles) {
        boolean isSuperAdmin = roles.contains("SUPER_ADMIN");
        boolean isOwner = store.owner != null && store.owner.id.equals(userId);

        if (!isSuperAdmin && !isOwner) {
            throw new ForbiddenException("You don't have permission to modify this store");
        }
    }
}
