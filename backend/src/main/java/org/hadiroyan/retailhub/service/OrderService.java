package org.hadiroyan.retailhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
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
import org.jboss.logging.Logger;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderService {

    private static final Logger LOG = Logger.getLogger(OrderService.class);

    @Inject
    OrderRepository orderRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    OrderMapper orderMapper;

    // CREATE — Customer
    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        LOG.debugf("action=CREATE_ORDER_START email=%s storeId=%s items=%d",
                email, request.storeId, request.items.size());

        User customer = findUserOrThrow(email);
        Store store = findStoreOrThrow(request.storeId);

        // Validate store is ACTIVE
        if (!"ACTIVE".equals(store.status)) {
            LOG.warnf("action=CREATE_ORDER_FAILED_STORE_NOT_ACTIVE storeId=%s customerId=%s",
                    store.id, customer.id);
            throw new BadRequestException("Store is not available");
        }

        SalesOrder order = new SalesOrder();
        order.customer = customer;
        order.store = store;
        order.orderNumber = generateOrderNumber();
        order.status = OrderStatus.PENDING.name();
        order.recipientName = request.recipientName;
        order.phone = request.phone;
        order.shippingAddress = request.shippingAddress;
        order.notes = request.notes;
        order.totalAmount = BigDecimal.ZERO;

        orderRepository.persist(order);

        // Add items
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemRequest : request.items) {
            Product product = findProductOrThrow(itemRequest.productId);
            validateProductBelongsToStore(product, store);

            if (!"ACTIVE".equals(product.status)) {
                LOG.warnf("action=CREATE_ORDER_FAILED_PRODUCT_NOT_ACTIVE productId=%s productStatus=%s",
                        product.id, product.status);
                throw new BadRequestException("Product '" + product.name + "' is not available");
            }

            SalesOrderItem item = new SalesOrderItem();
            item.salesOrder = order;
            item.product = product;
            item.quantity = itemRequest.quantity;
            item.unitPrice = product.price;
            item.subtotal = product.price.multiply(BigDecimal.valueOf(itemRequest.quantity));

            order.items.add(item);
            total = total.add(item.subtotal);
        }

        order.totalAmount = total;

        LOG.infof("action=CREATE_ORDER_SUCCESS email=%s orderId=%s orderNumber=%s total=%s",
                email, order.id, order.orderNumber, total);

        return orderMapper.toResponse(order);
    }

    // LIST — Customer
    public PagedResponse<OrderResponse> listCustomerOrders(UUID customerId, int page, int size) {
        LOG.debugf("action=LIST_CUSTOMER_ORDERS_START customerId=%s page=%d size=%d",
                customerId, page, size);

        List<SalesOrder> orders = orderRepository.findByCustomer(customerId, page, size);
        long total = orderRepository.countByCustomer(customerId);

        List<OrderResponse> content = orders.stream().map(orderMapper::toResponse).toList();
        LOG.infof("action=LIST_CUSTOMER_ORDERS_SUCCESS customerId=%s total=%d",
                customerId, total);

        return new PagedResponse<>(content, page, size, total);
    }

    // DETAIL — Customer and Store
    public OrderResponse getOrderDetail(UUID orderId, UUID requesterId, boolean isCustomer) {
        SalesOrder order = findOrderOrThrow(orderId);

        if (isCustomer) {
            // Customers can only view their own orders
            if (!order.customer.id.equals(requesterId)) {
                LOG.warnf("action=GET_ORDER_DETAIL_DENIED_ACCESS customerId=%s orderId=%s orderCustomerId=%s",
                        requesterId, orderId, order.customer.id);
                throw new ForbiddenException("You don't have access to this order");
            }
        } else {
            // Store staff — cek permission
            checkStorePermission(requesterId, order.store.id);
        }

        return orderMapper.toResponse(order);
    }

    // CANCEL — Customer
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID customerId) {
        LOG.debugf("action=CANCEL_ORDER_START customerId=%s orderId=%s", customerId, orderId);

        SalesOrder order = findOrderOrThrow(orderId);

        if (!order.customer.id.equals(customerId)) {
            LOG.warnf("action=CANCEL_ORDER_DENIED_ACCESS customerId=%s orderId=%s orderCustomerId=%s",
                    customerId, orderId, order.customer.id);
            throw new ForbiddenException("You don't have access to this order");
        }

        OrderStatus currentStatus = OrderStatus.valueOf(order.status);

        if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.PROCESSING) {
            LOG.warnf("action=CANCEL_ORDER_DENIED customerId=%s orderId=%s statusOrder=%s",
                    customerId, orderId, currentStatus);
            throw new BadRequestException(
                    "Order cannot be cancelled. Only PENDING or PROCESSING orders can be cancelled");
        }

        // Refund the stock if the order is canceled while in processing
        if (currentStatus == OrderStatus.PROCESSING) {
            restoreStock(order);
        }

        order.status = OrderStatus.CANCELLED.name();

        LOG.infof("action=CANCEL_ORDER_SUCCESS customerId=%s orderId=%s", customerId, orderId);

        return orderMapper.toResponse(order);
    }

    // LIST — Store
    public PagedResponse<OrderResponse> listStoreOrders(UUID storeId, UUID requesterId,
            String status, int page, int size) {

        LOG.debugf("action=LIST_STORE_ORDERS_START storeId=%s requesterId=%s status=%s page=%d size=%d",
                storeId, requesterId, status, page, size);

        findStoreOrThrow(storeId);
        checkStorePermission(requesterId, storeId);

        List<SalesOrder> orders = orderRepository.findByStore(storeId, status, page, size);
        long total = orderRepository.countByStore(storeId, status);

        List<OrderResponse> content = orders.stream().map(orderMapper::toResponse).toList();

        LOG.infof("action=LIST_STORE_ORDERS_SUCCESS storeId=%s requesterId=%s status=%s page=%d size=%d",
                storeId, requesterId, status, page, size);
        return new PagedResponse<>(content, page, size, total);
    }

    // UPDATE STATUS — Store
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, UUID requesterId,
            UpdateOrderStatusRequest request) {

        LOG.debugf("action=UPDATE_ORDER_STATUS_START requesterId=%s orderId=%s status=%s",
                requesterId, orderId, request.status);

        SalesOrder order = findOrderOrThrow(orderId);
        checkStorePermission(requesterId, order.store.id);

        OrderStatus currentStatus = OrderStatus.valueOf(order.status);
        OrderStatus newStatus = OrderStatus.valueOf(request.status);

        validateStatusTransition(orderId, currentStatus, newStatus);

        // Reduce stock during processing
        if (newStatus == OrderStatus.PROCESSING) {
            deductStock(order);
        }

        // Refund the stock if the order is canceled while in processing
        if (newStatus == OrderStatus.CANCELLED && currentStatus == OrderStatus.PROCESSING) {
            restoreStock(order);
        }

        order.status = newStatus.name();

        // Set tracking number if status SHIPPED
        if (newStatus == OrderStatus.SHIPPED && request.trackingNumber != null) {
            order.trackingNumber = request.trackingNumber;
        }

        LOG.infof("action=UPDATE_ORDER_STATUS_SUCCESS requesterId=%s orderId=%s status=%s",
                requesterId, orderId, newStatus);

        return orderMapper.toResponse(order);
    }

    // HELPERS
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "TRX-" + timestamp + "-" + random;
    }

    private void validateStatusTransition(UUID orderId, OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            LOG.warnf("action=INVALID_STATUS_TRANSITION orderId=%s currentStatus=%s nextStatus=%s",
                    orderId, current, next);
            throw new BadRequestException("Invalid status transition from " + current + " to " + next);
        }
    }

    private void deductStock(SalesOrder order) {
        for (SalesOrderItem item : order.items) {
            Product product = item.product;
            if (product.stockQuantity < item.quantity) {
                LOG.warnf("action=INSUFFICIENT_STOCK productId=%s stock=%d requested=%d",
                        product.id,
                        product.stockQuantity,
                        item.quantity);
                throw new BadRequestException("Insufficient stock for product: " + product.name);
            }
            product.stockQuantity -= item.quantity;
            LOG.debugf("action=STOCK_DEDUCTED productId=%s quantity=%d remaining=%d",
                    product.id,
                    item.quantity,
                    product.stockQuantity);
        }
    }

    private void restoreStock(SalesOrder order) {
        for (SalesOrderItem item : order.items) {
            item.product.stockQuantity += item.quantity;
            LOG.debugf("action=STOCK_RESTORED productId=%s quantity=%d restored=%d",
                    item.product.id,
                    item.quantity,
                    item.product.stockQuantity);
        }
    }

    private void validateProductBelongsToStore(Product product, Store store) {
        if (!product.store.id.equals(store.id)) {
            LOG.warnf("action=PRODUCT_STORE_MISMATCH productId=%s productStoreId=%s requestedStoreId=%s",
                    product.id, product.store.id, store.id);
            throw new BadRequestException("Product '" + product.name + "' does not belong to this store");
        }
    }

    private void checkStorePermission(UUID userId, UUID storeId) {
        boolean hasPermission = userRoleRepository.userHasAnyRoleInStore(
                userId,
                Set.of("OWNER", "ADMIN", "MANAGER"),
                storeId);
        if (!hasPermission) {
            LOG.warnf("action=WRITE_PERMISSION_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("No permission to manage this store's orders");
        }
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("action=USER_NOT_FOUND email=%s", email);
                    return new NotFoundException("User not found");
                });
    }

    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findByIdOptional(productId)
                .orElseThrow(() -> {
                    LOG.warnf("action=PRODUCT_NOT_FOUND productId=%s", productId);
                    return new NotFoundException("Product not found: " + productId);
                });
    }

    private SalesOrder findOrderOrThrow(UUID orderId) {
        return orderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> {
                    LOG.warnf("action=ORDER_NOT_FOUND orderId=%s", orderId);
                    return new NotFoundException("Order not found");
                });
    }

}