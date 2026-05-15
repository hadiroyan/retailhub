package org.hadiroyan.retailhub.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateSupplierRequest;
import org.hadiroyan.retailhub.dto.request.UpdateSupplierRequest;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.SupplierResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.SupplierMapper;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.Supplier;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.SupplierRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.jboss.logging.Logger;

import io.quarkus.logging.Log;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SupplierService {

    private static final Logger LOG = Logger.getLogger(SupplierService.class);

    @Inject
    SupplierRepository supplierRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    SupplierMapper supplierMapper;

    // CREATE
    @Transactional
    public SupplierResponse createSupplier(UUID storeId, UUID userId, CreateSupplierRequest request) {
        LOG.debugf("action=CREATE_SUPPLIER_START userId=%s storeId=%s name=%s",
                userId, storeId, request.name);

        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        if (supplierRepository.existsByNameAndStore(storeId, request.name)) {
            LOG.warnf("action=CREATE_SUPPLIER_DUPLICATE userId=%s storeId=%s name=%s",
                    userId, storeId, request.name);
            throw new BadRequestException("Supplier with this name already exists");
        }

        Supplier supplier = new Supplier();
        supplier.store = store;
        supplier.name = request.name;
        supplier.contactPerson = request.contactPerson;
        supplier.email = request.email;
        supplier.phone = request.phone;
        supplier.address = request.address;
        supplier.notes = request.notes;

        supplierRepository.persist(supplier);

        LOG.infof("action=CREATE_SUPPLIER_SUCCESS userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplier.id);

        return supplierMapper.toResponse(supplier);
    }

    // LIST
    public PagedResponse<SupplierResponse> listSuppliers(UUID storeId, UUID userId,
            int page, int size) {
        LOG.debugf("action=LIST_SUPPLIERS_START userId=%s storeId=%s", userId, storeId);

        findStoreOrThrow(storeId);
        checkReadPermission(userId, storeId);

        List<Supplier> suppliers = supplierRepository.findByStore(storeId, page, size);
        long total = supplierRepository.countByStore(storeId);

        LOG.infof("action=LIST_SUPPLIERS_SUCCCESS userId=%s storeId=%s", userId, storeId);
        return new PagedResponse<>(
                suppliers.stream().map(supplierMapper::toResponse).toList(),
                page, size, total);
    }

    // DETAIL
    public SupplierResponse getSupplier(UUID storeId, UUID supplierId, UUID userId) {
        LOG.debugf("action=GET_SUPPLIER_START userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        findStoreOrThrow(storeId);
        checkReadPermission(userId, storeId);

        Supplier supplier = findSupplierOrThrow(supplierId, storeId);

        LOG.infof("action=GET_SUPPLIER_SUCCESS userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        return supplierMapper.toResponse(supplier);
    }

    // UPDATE
    @Transactional
    public SupplierResponse updateSupplier(UUID storeId, UUID supplierId,
            UUID userId, UpdateSupplierRequest request) {
        LOG.debugf("action=UPDATE_SUPPLIER_START userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Supplier supplier = findSupplierOrThrow(supplierId, storeId);

        // Check for duplicate names only if the name has changed
        if (!supplier.name.equalsIgnoreCase(request.name) &&
                supplierRepository.existsByNameAndStore(storeId, request.name)) {
            Log.warnf("action=UPDATE_SUPPLIER_DENIED_DUPLICATE_NAME userId=%s supplierId=%s storeId=%s", userId,
                    supplierId, storeId);
            throw new BadRequestException("Supplier with this name already exists");
        }

        supplier.name = request.name;
        supplier.contactPerson = request.contactPerson;
        supplier.email = request.email;
        supplier.phone = request.phone;
        supplier.address = request.address;
        supplier.notes = request.notes;

        LOG.infof("action=UPDATE_SUPPLIER_SUCCESS userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        return supplierMapper.toResponse(supplier);
    }

    // DELETE
    @Transactional
    public void deleteSupplier(UUID storeId, UUID supplierId, UUID userId) {
        LOG.debugf("action=DELETE_SUPPLIER_START userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Supplier supplier = findSupplierOrThrow(supplierId, storeId);
        supplierRepository.delete(supplier);

        LOG.infof("action=DELETE_SUPPLIER_SUCCESS userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);
    }

    // Helpers
    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });
    }

    private Supplier findSupplierOrThrow(UUID supplierId, UUID storeId) {
        return supplierRepository.findByIdAndStore(supplierId, storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=SUPPLIER_NOT_FOUND supplierId=%s storeId=%s",
                            supplierId, storeId);
                    return new NotFoundException("Supplier not found");
                });
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN"), storeId);
        if (!canWrite) {
            LOG.warnf("action=SUPPLIER_WRITE_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("No permission to manage suppliers");
        }
    }

    private void checkReadPermission(UUID userId, UUID storeId) {
        boolean canRead = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);
        if (!canRead) {
            LOG.warnf("action=SUPPLIER_READ_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("No permission to view suppliers");
        }
    }
}