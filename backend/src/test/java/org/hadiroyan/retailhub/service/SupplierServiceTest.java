package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.security.ForbiddenException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class SupplierServiceTest {

    @Inject
    SupplierService supplierService;

    @InjectMock
    SupplierRepository supplierRepository;

    @InjectMock
    StoreRepository storeRepository;

    @InjectMock
    UserRoleRepository userRoleRepository;

    @InjectMock
    SupplierMapper supplierMapper;

    UUID storeId = UUID.randomUUID();
    UUID supplierId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Store mockStore;
    Supplier mockSupplier;
    SupplierResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockStore = new Store();
        mockStore.id = storeId;
        mockStore.name = "Test Store";

        mockSupplier = new Supplier();
        mockSupplier.id = supplierId;
        mockSupplier.store = mockStore;
        mockSupplier.name = "Test Supplier";
        mockSupplier.phone = "08123456789";

        mockResponse = new SupplierResponse();
        mockResponse.id = supplierId;
        mockResponse.name = "Test Supplier";
    }

    // =========================================================================
    // createSupplier()
    // =========================================================================

    @Test
    void should_create_supplier_success() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.name = "Test Supplier";
        request.phone = "08123456789";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.existsByNameAndStore(storeId, request.name)).thenReturn(false);
        doNothing().when(supplierRepository).persist(any(Supplier.class));
        when(supplierMapper.toResponse(any(Supplier.class))).thenReturn(mockResponse);

        SupplierResponse result = supplierService.createSupplier(storeId, userId, request);

        assertNotNull(result);
        assertEquals("Test Supplier", result.name);
        verify(supplierRepository).persist(any(Supplier.class));
    }

    @Test
    void should_throw_BadRequestException_when_supplier_name_already_exists() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.name = "Test Supplier";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.existsByNameAndStore(storeId, request.name)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> supplierService.createSupplier(storeId, userId, request));
        verify(supplierRepository, never()).persist(any(Supplier.class));
    }

    @Test
    void should_throw_ForbiddenException_when_no_write_permission() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.name = "Test Supplier";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> supplierService.createSupplier(storeId, userId, request));
    }

    @Test
    void should_throw_NotFoundException_when_store_not_found() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.name = "Test Supplier";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> supplierService.createSupplier(storeId, userId, request));
    }

    // =========================================================================
    // listSuppliers()
    // =========================================================================

    @Test
    void should_list_suppliers_success() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByStore(storeId, 0, 10)).thenReturn(List.of(mockSupplier));
        when(supplierRepository.countByStore(storeId)).thenReturn(1L);
        when(supplierMapper.toResponse(mockSupplier)).thenReturn(mockResponse);

        PagedResponse<SupplierResponse> result = supplierService.listSuppliers(storeId, userId, 0, 10);

        assertEquals(1, result.content.size());
        assertEquals(1, result.totalElements);
    }

    @Test
    void should_throw_ForbiddenException_when_no_read_permission() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> supplierService.listSuppliers(storeId, userId, 0, 10));
    }

    // =========================================================================
    // getSupplier()
    // =========================================================================

    @Test
    void should_get_supplier_success() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.of(mockSupplier));
        when(supplierMapper.toResponse(mockSupplier)).thenReturn(mockResponse);

        SupplierResponse result = supplierService.getSupplier(storeId, supplierId, userId);

        assertNotNull(result);
        assertEquals(supplierId, result.id);
    }

    @Test
    void should_throw_NotFoundException_when_supplier_not_found() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> supplierService.getSupplier(storeId, supplierId, userId));
    }

    // =========================================================================
    // updateSupplier()
    // =========================================================================

    @Test
    void should_update_supplier_success() {
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.name = "Updated Supplier";
        request.phone = "08987654321";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.of(mockSupplier));
        when(supplierRepository.existsByNameAndStore(storeId, request.name)).thenReturn(false);
        when(supplierMapper.toResponse(mockSupplier)).thenReturn(mockResponse);

        SupplierResponse result = supplierService.updateSupplier(storeId, supplierId, userId, request);

        assertNotNull(result);
        assertEquals("Updated Supplier", mockSupplier.name);
    }

    @Test
    void should_throw_BadRequestException_when_update_with_duplicate_name() {
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        request.name = "Other Supplier"; // nama berbeda tapi sudah ada di store

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.of(mockSupplier));
        when(supplierRepository.existsByNameAndStore(storeId, request.name)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> supplierService.updateSupplier(storeId, supplierId, userId, request));
    }

    // =========================================================================
    // deleteSupplier()
    // =========================================================================

    @Test
    void should_delete_supplier_success() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.of(mockSupplier));
        doNothing().when(supplierRepository).delete(mockSupplier);

        assertDoesNotThrow(() -> supplierService.deleteSupplier(storeId, supplierId, userId));
        verify(supplierRepository).delete(mockSupplier);
    }

    @Test
    void should_throw_NotFoundException_when_delete_non_existing_supplier() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> supplierService.deleteSupplier(storeId, supplierId, userId));
        verify(supplierRepository, never()).delete(any());
    }
}