package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateOrderItemRequest;
import org.hadiroyan.retailhub.dto.request.CreateOrderRequest;
import org.hadiroyan.retailhub.dto.request.UpdateOrderStatusRequest;
import org.hadiroyan.retailhub.dto.response.OrderResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.OrderMapper;
import org.hadiroyan.retailhub.model.OrderStatus;
import org.hadiroyan.retailhub.model.Product;
import org.hadiroyan.retailhub.model.SalesOrder;
import org.hadiroyan.retailhub.model.SalesOrderItem;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.repository.OrderRepository;
import org.hadiroyan.retailhub.repository.ProductRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.security.ForbiddenException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class OrderServiceTest {

    @Inject
    OrderService orderService;

    @InjectMock
    OrderRepository orderRepository;

    @InjectMock
    ProductRepository productRepository;

    @InjectMock
    StoreRepository storeRepository;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    UserRoleRepository userRoleRepository;

    @InjectMock
    OrderMapper orderMapper;

    UUID customerId = UUID.randomUUID();
    UUID storeId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    String customerEmail = "customer@test.com";

    Store mockStore;
    User mockCustomer;
    Product mockProduct;
    SalesOrder mockOrder;
    OrderResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockStore = new Store();
        mockStore.id = storeId;
        mockStore.name = "Test Store";
        mockStore.status = "ACTIVE";

        mockCustomer = new User();
        mockCustomer.id = customerId;
        mockCustomer.email = customerEmail;

        mockProduct = new Product();
        mockProduct.id = productId;
        mockProduct.name = "Test Product";
        mockProduct.price = BigDecimal.valueOf(100000);
        mockProduct.status = "ACTIVE";
        mockProduct.stockQuantity = 10;
        mockProduct.store = mockStore;
        mockProduct.imageUrls = new ArrayList<>();

        mockOrder = new SalesOrder();
        mockOrder.id = orderId;
        mockOrder.customer = mockCustomer;
        mockOrder.store = mockStore;
        mockOrder.status = OrderStatus.PENDING.name();
        mockOrder.totalAmount = BigDecimal.valueOf(100000);
        mockOrder.items = new ArrayList<>();

        mockResponse = new OrderResponse();
        mockResponse.id = orderId;
        mockResponse.status = OrderStatus.PENDING.name();
    }

    // createOrder()
    @Test
    void should_create_order_success() {
        CreateOrderRequest request = buildCreateOrderRequest(1);

        when(userRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(mockProduct));
        doNothing().when(orderRepository).persist(any(SalesOrder.class));
        when(orderMapper.toResponse(any(SalesOrder.class))).thenReturn(mockResponse);

        OrderResponse result = orderService.createOrder(customerEmail, request);

        assertNotNull(result);
        verify(orderRepository).persist(any(SalesOrder.class));
    }

    @Test
    void should_throw_BadRequestException_when_store_is_not_active() {
        mockStore.status = "CLOSED";
        CreateOrderRequest request = buildCreateOrderRequest(1);

        when(userRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));

        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(customerEmail, request));
        verify(orderRepository, never()).persist(any(SalesOrder.class));
    }

    @Test
    void should_throw_BadRequestException_when_product_is_not_active() {
        mockProduct.status = "DRAFT";
        CreateOrderRequest request = buildCreateOrderRequest(1);

        when(userRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.of(mockStore));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(mockProduct));
        doNothing().when(orderRepository).persist(any(SalesOrder.class));

        assertThrows(BadRequestException.class,
                () -> orderService.createOrder(customerEmail, request));
    }

    @Test
    void should_throw_NotFoundException_when_store_not_found() {
        CreateOrderRequest request = buildCreateOrderRequest(1);

        when(userRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(storeRepository.findByIdOptional(storeId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.createOrder(customerEmail, request));
    }

    // listCustomerOrders()
    @Test
    void should_list_customer_orders_success() {
        when(orderRepository.findByCustomer(customerId, 0, 10)).thenReturn(List.of(mockOrder));
        when(orderRepository.countByCustomer(customerId)).thenReturn(1L);
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        PagedResponse<OrderResponse> result = orderService.listCustomerOrders(customerId, 0, 10);

        assertEquals(1, result.content.size());
        assertEquals(1, result.totalElements);
    }

    // getOrderDetail()
    @Test
    void should_get_order_detail_success_for_customer() {
        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        OrderResponse result = orderService.getOrderDetail(orderId, customerId, true);

        assertNotNull(result);
    }

    @Test
    void should_throw_ForbiddenException_when_customer_access_other_order() {
        UUID otherCustomerId = UUID.randomUUID();
        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));

        assertThrows(ForbiddenException.class,
                () -> orderService.getOrderDetail(orderId, otherCustomerId, true));
    }

    // cancelOrder()
    @Test
    void should_cancel_order_success_when_status_is_pending() {
        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        OrderResponse result = orderService.cancelOrder(orderId, customerId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED.name(), mockOrder.status);
    }

    @Test
    void should_throw_BadRequestException_when_cancel_shipped_order() {
        mockOrder.status = OrderStatus.SHIPPED.name();
        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));

        assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(orderId, customerId));
    }

    @Test
    void should_restore_stock_when_cancel_processing_order() {
        mockOrder.status = OrderStatus.PROCESSING.name();
        SalesOrderItem item = buildOrderItem(mockProduct, 3);
        mockOrder.items.add(item);
        int originalStock = mockProduct.stockQuantity;

        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        orderService.cancelOrder(orderId, customerId);

        assertEquals(originalStock + 3, mockProduct.stockQuantity);
    }

    // updateOrderStatus()
    @Test
    void should_update_status_to_processing_and_deduct_stock() {
        mockOrder.status = OrderStatus.PENDING.name();
        SalesOrderItem item = buildOrderItem(mockProduct, 2);
        mockOrder.items.add(item);
        int originalStock = mockProduct.stockQuantity;

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.status = "PROCESSING";

        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(userRoleRepository.userHasAnyRoleInStore(any(), any(), any())).thenReturn(true);
        when(orderMapper.toResponse(mockOrder)).thenReturn(mockResponse);

        orderService.updateOrderStatus(orderId, UUID.randomUUID(), request);

        assertEquals(originalStock - 2, mockProduct.stockQuantity);
    }

    @Test
    void should_throw_BadRequestException_when_invalid_status_transition() {
        mockOrder.status = OrderStatus.DELIVERED.name();

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.status = "PROCESSING";

        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(userRoleRepository.userHasAnyRoleInStore(any(), any(), any())).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(orderId, UUID.randomUUID(), request));
    }

    @Test
    void should_throw_BadRequestException_when_insufficient_stock() {
        mockOrder.status = OrderStatus.PENDING.name();
        mockProduct.stockQuantity = 1;
        SalesOrderItem item = buildOrderItem(mockProduct, 5); // request 5, stock only 1
        mockOrder.items.add(item);

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.status = "PROCESSING";

        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(userRoleRepository.userHasAnyRoleInStore(any(), any(), any())).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> orderService.updateOrderStatus(orderId, UUID.randomUUID(), request));
    }

    @Test
    void should_throw_ForbiddenException_when_no_store_permission() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.status = "PROCESSING";

        when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(mockOrder));
        when(userRoleRepository.userHasAnyRoleInStore(any(), any(), any())).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> orderService.updateOrderStatus(orderId, UUID.randomUUID(), request));
    }

    // Helpers
    private CreateOrderRequest buildCreateOrderRequest(int quantity) {
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.productId = productId;
        itemRequest.quantity = quantity;

        CreateOrderRequest request = new CreateOrderRequest();
        request.storeId = storeId;
        request.recipientName = "John Doe";
        request.phone = "08123456789";
        request.shippingAddress = "Jl. Sudirman No. 1";
        request.items = List.of(itemRequest);

        return request;
    }

    private SalesOrderItem buildOrderItem(Product product, int quantity) {
        SalesOrderItem item = new SalesOrderItem();
        item.product = product;
        item.quantity = quantity;
        item.unitPrice = product.price;
        item.subtotal = product.price.multiply(BigDecimal.valueOf(quantity));
        return item;
    }
}
