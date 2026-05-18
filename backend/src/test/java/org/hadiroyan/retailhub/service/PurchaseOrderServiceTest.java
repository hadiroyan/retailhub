package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreatePurchaseOrderItemRequest;
import org.hadiroyan.retailhub.dto.request.CreatePurchaseOrderRequest;
import org.hadiroyan.retailhub.dto.request.UpdatePurchaseOrderStatusRequest;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.PurchaseOrderResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.PurchaseOrderMapper;
import org.hadiroyan.retailhub.model.Product;
import org.hadiroyan.retailhub.model.PurchaseOrder;
import org.hadiroyan.retailhub.model.PurchaseOrderItem;
import org.hadiroyan.retailhub.model.PurchaseOrderStatus;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.Supplier;
import org.hadiroyan.retailhub.repository.ProductRepository;
import org.hadiroyan.retailhub.repository.PurchaseOrderRepository;
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
public class PurchaseOrderServiceTest {

    @Inject
    PurchaseOrderService purchaseOrderService;

    @InjectMock
    PurchaseOrderRepository purchaseOrderRepository;

    @InjectMock
    SupplierRepository supplierRepository;

    @InjectMock
    StoreRepository storeRepository;

    @InjectMock
    ProductRepository productRepository;

    @InjectMock
    UserRoleRepository userRoleRepository;

    @InjectMock
    PurchaseOrderMapper purchaseOrderMapper;

    UUID storeId = UUID.randomUUID();
    UUID supplierId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Store mockStore;
    Supplier mockSupplier;
    Product mockProduct;
    PurchaseOrder mockOrder;
    PurchaseOrderResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockStore = new Store();
        mockStore.id = storeId;
        mockStore.name = "Test Store";

        mockSupplier = new Supplier();
        mockSupplier.id = supplierId;
        mockSupplier.store = mockStore;
        mockSupplier.name = "Test Supplier";

        mockProduct = new Product();
        mockProduct.id = productId;
        mockProduct.name = "Test Product";
        mockProduct.price = BigDecimal.valueOf(50000);
        mockProduct.stockQuantity = 10;
        mockProduct.store = mockStore;

        mockOrder = new PurchaseOrder();
        mockOrder.id = orderId;
        mockOrder.store = mockStore;
        mockOrder.supplier = mockSupplier;
        mockOrder.status = PurchaseOrderStatus.PENDING.name();
        mockOrder.totalAmount = BigDecimal.valueOf(500000);
        mockOrder.items = new ArrayList<>();

        mockResponse = new PurchaseOrderResponse();
        mockResponse.id = orderId;
        mockResponse.status = PurchaseOrderStatus.PENDING.name();
    }

    // =========================================================================
    // createPurchaseOrder()
    // =========================================================================

    @Test
    void should_create_purchase_order_success() {
        CreatePurchaseOrderRequest request = buildCreateRequest(2);

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.of(mockSupplier));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(mockProduct));
        doNothing().when(purchaseOrderRepository).persist(any(PurchaseOrder.class));
        when(purchaseOrderMapper.toResponse(any(PurchaseOrder.class))).thenReturn(mockResponse);

        PurchaseOrderResponse result = purchaseOrderService.createPurchaseOrder(storeId, userId, request);

        assertNotNull(result);
        verify(purchaseOrderRepository).persist(any(PurchaseOrder.class));
    }

    @Test
    void should_throw_NotFoundException_when_supplier_not_found() {
        CreatePurchaseOrderRequest request = buildCreateRequest(1);

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> purchaseOrderService.createPurchaseOrder(storeId, userId, request));
        verify(purchaseOrderRepository, never()).persist(any(PurchaseOrder.class));
    }

    @Test
    void should_throw_ForbiddenException_when_no_write_permission() {
        CreatePurchaseOrderRequest request = buildCreateRequest(1);

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> purchaseOrderService.createPurchaseOrder(storeId, userId, request));
    }

    @Test
    void should_throw_BadRequestException_when_product_belongs_to_different_store() {
        Store otherStore = new Store();
        otherStore.id = UUID.randomUUID();
        mockProduct.store = otherStore; // products from other stores

        CreatePurchaseOrderRequest request = buildCreateRequest(1);

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(supplierRepository.findByIdAndStore(supplierId, storeId)).thenReturn(Optional.of(mockSupplier));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(mockProduct));
        doNothing().when(purchaseOrderRepository).persist(any(PurchaseOrder.class));

        assertThrows(BadRequestException.class,
                () -> purchaseOrderService.createPurchaseOrder(storeId, userId, request));
    }

    // =========================================================================
    // listPurchaseOrders()
    // =========================================================================

    @Test
    void should_list_purchase_orders_success() {
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(purchaseOrderRepository.findByStore(storeId, null, 0, 10)).thenReturn(List.of(mockOrder));
        when(purchaseOrderRepository.countByStore(storeId, null)).thenReturn(1L);
        when(purchaseOrderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        PagedResponse<PurchaseOrderResponse> result = purchaseOrderService.listPurchaseOrders(storeId, userId, null, 0,
                10);

        assertEquals(1, result.content.size());
        assertEquals(1, result.totalElements);
    }

    // =========================================================================
    // updateStatus()
    // =========================================================================

    @Test
    void should_update_status_pending_to_confirmed_success() {
        UpdatePurchaseOrderStatusRequest request = new UpdatePurchaseOrderStatusRequest();
        request.status = "CONFIRMED";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(purchaseOrderRepository.findByIdAndStore(orderId, storeId)).thenReturn(Optional.of(mockOrder));
        when(purchaseOrderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        purchaseOrderService.updateStatus(storeId, orderId, userId, request);

        assertEquals(PurchaseOrderStatus.CONFIRMED.name(), mockOrder.status);
    }

    @Test
    void should_add_stock_when_status_changed_to_received() {
        mockOrder.status = PurchaseOrderStatus.CONFIRMED.name();
        PurchaseOrderItem item = buildOrderItem(mockProduct, 5);
        mockOrder.items.add(item);
        int originalStock = mockProduct.stockQuantity;

        UpdatePurchaseOrderStatusRequest request = new UpdatePurchaseOrderStatusRequest();
        request.status = "RECEIVED";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(purchaseOrderRepository.findByIdAndStore(orderId, storeId)).thenReturn(Optional.of(mockOrder));
        when(purchaseOrderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        purchaseOrderService.updateStatus(storeId, orderId, userId, request);

        assertEquals(originalStock + 5, mockProduct.stockQuantity);
    }

    @Test
    void should_throw_BadRequestException_when_invalid_status_transition() {
        mockOrder.status = PurchaseOrderStatus.RECEIVED.name(); // final state

        UpdatePurchaseOrderStatusRequest request = new UpdatePurchaseOrderStatusRequest();
        request.status = "CONFIRMED";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(purchaseOrderRepository.findByIdAndStore(orderId, storeId)).thenReturn(Optional.of(mockOrder));

        assertThrows(BadRequestException.class,
                () -> purchaseOrderService.updateStatus(storeId, orderId, userId, request));
    }

    @Test
    void should_throw_BadRequestException_when_cancelled_to_any_status() {
        mockOrder.status = PurchaseOrderStatus.CANCELLED.name();

        UpdatePurchaseOrderStatusRequest request = new UpdatePurchaseOrderStatusRequest();
        request.status = "PENDING";

        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(userRoleRepository.userHasAnyRoleInStore(eq(userId), any(), eq(storeId))).thenReturn(true);
        when(purchaseOrderRepository.findByIdAndStore(orderId, storeId)).thenReturn(Optional.of(mockOrder));

        assertThrows(BadRequestException.class,
                () -> purchaseOrderService.updateStatus(storeId, orderId, userId, request));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CreatePurchaseOrderRequest buildCreateRequest(int quantity) {
        CreatePurchaseOrderItemRequest itemRequest = new CreatePurchaseOrderItemRequest();
        itemRequest.productId = productId;
        itemRequest.quantity = quantity;
        itemRequest.unitPrice = BigDecimal.valueOf(50000);

        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.supplierId = supplierId;
        request.items = List.of(itemRequest);

        return request;
    }

    private PurchaseOrderItem buildOrderItem(Product product, int quantity) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.product = product;
        item.quantity = quantity;
        item.unitPrice = product.price;
        item.subtotal = product.price.multiply(BigDecimal.valueOf(quantity));
        return item;
    }
}