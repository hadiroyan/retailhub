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
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StoreService {

    private static final Logger LOG = Logger.getLogger(StoreService.class);

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
        LOG.debugf("action=CREATE_STORE_START email=%s storeName=%s",
                email, request.name);

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });

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

        LOG.infof("action=CREATE_STORE_SUCCESS userId=%s storeId=%s storeName=%s slug=%s",
                owner.id, store.id, store.name, store.slug);

        return storeMapper.toResponse(store);
    }

    private void assignOwnerRole(User owner, Store store) {
        LOG.debugf("action=ASSIGN_OWNER_ROLE_START userId=%s storeId=%s",
                owner.id, store.id);

        Role role = roleRepository.findByName("OWNER")
                .orElseThrow(() -> {
                    LOG.error("action=ROLE_NOT_FOUND role=OWNER");
                    return new NotFoundException("Role not found");
                });

        UserRole userRole = new UserRole();
        userRole.user = owner;
        userRole.storeId = store.id;
        userRole.role = role;

        userRoleRepository.persist(userRole);
        LOG.infof("action=ASSIGN_OWNER_ROLE_SUCCESS userId=%s storeId=%s role=OWNER",
                owner.id, store.id);
    }

    public PagedResponse<StoreResponse> listsStores(UUID userId, Set<String> roles, int page, int size) {

        LOG.debugf("action=LIST_STORES_START userId=%s roles=%s page=%d size=%d",
                userId, roles, page, size);

        List<Store> stores;
        long total;
        String accessType;

        if (roles.contains("SUPER_ADMIN")) {
            accessType = "SUPER_ADMIN";
            stores = storeRepository.findAllPaged(page, size);
            total = storeRepository.countAll();
        } else if (roles.contains("OWNER")) {
            accessType = "OWNER";
            stores = storeRepository.findByOwner(userId, page, size);
            total = storeRepository.countByOwner(userId);
        } else {
            accessType = "PUBLIC";
            stores = storeRepository.findAllActive(page, size);
            total = storeRepository.countAllActive();
        }

        List<StoreResponse> data = stores.stream()
                .map(storeMapper::toResponse)
                .toList();

        LOG.infof("action=LIST_STORES_SUCCESS userId=%s accessType=%s total=%d page=%d size=%d",
                userId, accessType, total, page, size);

        return new PagedResponse<>(data, page, size, total);
    }

    public StoreResponse getStoreBySlug(String slug, UUID userId, Set<String> roles) {
        LOG.debugf("action=GET_STORE_BY_SLUG_START userId=%s slug=%s",
                userId, slug);

        Store store = storeRepository.findBySlug(slug)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND slug=%s", slug);
                    return new NotFoundException("Store not found");
                });
        ;

        boolean isActive = StoreStatus.ACTIVE.name().equals(store.status);
        boolean isSuperAdmin = roles.contains("SUPER_ADMIN");
        boolean isOwner = store.owner != null && store.owner.id.equals(userId);
        LOG.infof("Get store by slug with isActive[%b] isSuperAdmin[%b] isOwner[%b]", isActive, isSuperAdmin, isOwner);

        if (!isActive && !isSuperAdmin && !isOwner) {
            LOG.warnf("action=STORE_ACCESS_DENIED userId=%s storeId=%s slug=%s",
                    userId, store.id, slug);
            throw new NotFoundException("Store not found");
        }

        LOG.infof("action=GET_STORE_BY_SLUG_SUCCESS userId=%s storeId=%s slug=%s",
                userId, store.id, slug);

        return storeMapper.toResponse(store);
    }

    @Transactional
    public StoreResponse updateStore(UUID storeId, UUID userId, Set<String> roles,
            UpdateStoreRequest request) {

        LOG.debugf("action=UPDATE_STORE_START userId=%s storeId=%s",
                userId, storeId);

        Store store = storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });

        assertCanModify(store, userId, roles);

        store.name = request.name;
        store.slug = slugUtil.generateUniqueSlugForUpdate(request.name, storeId);
        store.description = request.description;
        store.address = request.address;
        store.phone = request.phone;
        store.email = request.email;

        LOG.infof("action=UPDATE_STORE_SUCCESS userId=%s storeId=%s storeName=%s",
                userId, store.id, store.name);

        return storeMapper.toResponse(store);
    }

    @Transactional
    public void deleteStore(UUID storeId, UUID userId, Set<String> roles) {
        LOG.debugf("action=DELETE_STORE_START userId=%s storeId=%s",
                userId, storeId);

        Store store = storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });

        assertCanModify(store, userId, roles);
        storeRepository.delete(store);

        LOG.infof("action=DELETE_STORE_SUCCESS userId=%s storeId=%s",
                userId, storeId);
    }

    @Transactional
    public StoreResponse updateStatus(UUID storeId, UUID userId, Set<String> roles, UpdateStoreStatusRequest request) {
        LOG.debugf("action=UPDATE_STORE_STATUS_START userId=%s storeId=%s status=%s",
                userId, storeId, request.status);

        Store store = storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });

        assertCanModify(store, userId, roles);
        StoreStatus newStatus = StoreStatus.valueOf(request.status);

        // OWNER cannot set the SUSPEND status
        // This is the platform's right (SUPER_ADMIN)
        boolean isSuperAdmin = roles.contains("SUPER_ADMIN");
        if (!isSuperAdmin && newStatus == StoreStatus.SUSPEND) {
            LOG.warnf("action=FORBIDDEN_SUSPEND_STORE userId=%s storeId=%s",
                    userId, storeId);
            throw new ForbiddenException("Only SUPER_ADMIN can suspend a store");
        }

        store.status = newStatus.name();

        LOG.infof("action=UPDATE_STORE_STATUS_SUCCESS userId=%s storeId=%s status=%s",
                userId, storeId, newStatus.name());
        return storeMapper.toResponse(store);
    }

    private void assertCanModify(Store store, UUID userId, Set<String> roles) {
        boolean isSuperAdmin = roles.contains("SUPER_ADMIN");
        boolean isOwner = store.owner != null && store.owner.id.equals(userId);

        if (!isSuperAdmin && !isOwner) {
            LOG.warnf("action=STORE_MODIFY_DENIED userId=%s storeId=%s",
                    userId, store.id);
            throw new ForbiddenException("No permission");
        }
    }
}
