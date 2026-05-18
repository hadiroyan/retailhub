package org.hadiroyan.retailhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
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
import org.jboss.logging.Logger;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PurchaseOrderService {

    private static final Logger LOG = Logger.getLogger(PurchaseOrderService.class);

    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    SupplierRepository supplierRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    PurchaseOrderMapper purchaseOrderMapper;

    // CREATE
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(UUID storeId, UUID userId,
            CreatePurchaseOrderRequest request) {

        LOG.debugf("action=CREATE_PO_START userId=%s storeId=%s supplierId=%s items=%d",
                userId, storeId, request.supplierId, request.items.size());

        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Supplier supplier = supplierRepository.findByIdAndStore(request.supplierId, storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=SUPPLIER_NOT_FOUND supplierId=%s storeId=%s",
                            request.supplierId, storeId);
                    return new NotFoundException("Supplier not found");
                });

        PurchaseOrder order = new PurchaseOrder();
        order.store = store;
        order.supplier = supplier;
        order.orderNumber = generateOrderNumber();
        order.status = PurchaseOrderStatus.PENDING.name();
        order.expectedDeliveryDate = request.expectedDeliveryDate;
        order.notes = request.notes;
        order.totalAmount = BigDecimal.ZERO;

        purchaseOrderRepository.persist(order);

        // Add items
        BigDecimal total = BigDecimal.ZERO;
        for (CreatePurchaseOrderItemRequest itemRequest : request.items) {
            Product product = findProductOrThrow(itemRequest.productId);
            validateProductBelongsToStore(product, storeId);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.purchaseOrder = order;
            item.product = product;
            item.quantity = itemRequest.quantity;
            item.unitPrice = itemRequest.unitPrice;
            item.subtotal = itemRequest.unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity));

            order.items.add(item);
            total = total.add(item.subtotal);
        }

        order.totalAmount = total;

        LOG.infof("action=CREATE_PO_SUCCESS userId=%s storeId=%s orderId=%s orderNumber=%s total=%s",
                userId, storeId, order.id, order.orderNumber, total);

        return purchaseOrderMapper.toResponse(order);
    }

    // LIST
    public PagedResponse<PurchaseOrderResponse> listPurchaseOrders(UUID storeId, UUID userId,
            String status, int page, int size) {

        LOG.debugf("action=LIST_PO_START userId=%s storeId=%s status=%s", userId, storeId, status);

        findStoreOrThrow(storeId);
        checkReadPermission(userId, storeId);

        List<PurchaseOrder> orders = purchaseOrderRepository.findByStore(storeId, status, page, size);
        long total = purchaseOrderRepository.countByStore(storeId, status);

        LOG.infof("action=LIST_PO_SUCCESS userId=%s storeId=%s status=%s", userId, storeId, status);
        return new PagedResponse<>(
                orders.stream().map(purchaseOrderMapper::toResponse).toList(),
                page, size, total);
    }

    // DETAIL
    public PurchaseOrderResponse getPurchaseOrder(UUID storeId, UUID orderId, UUID userId) {
        LOG.debugf("action=GET_PO_START userId=%s storeId=%s orderId=%s", userId, storeId, orderId);

        findStoreOrThrow(storeId);
        checkReadPermission(userId, storeId);

        PurchaseOrder order = findPurchaseOrderOrThrow(orderId, storeId);

        LOG.infof("action=GET_PO_SUCCESS userId=%s storeId=%s orderId=%s", userId, storeId, orderId);
        return purchaseOrderMapper.toResponse(order);
    }

    // UPDATE STATUS
    @Transactional
    public PurchaseOrderResponse updateStatus(UUID storeId, UUID orderId, UUID userId,
            UpdatePurchaseOrderStatusRequest request) {

        LOG.debugf("action=UPDATE_PO_STATUS_START userId=%s storeId=%s orderId=%s status=%s",
                userId, storeId, orderId, request.status);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        PurchaseOrder order = findPurchaseOrderOrThrow(orderId, storeId);

        PurchaseOrderStatus currentStatus = PurchaseOrderStatus.valueOf(order.status);
        PurchaseOrderStatus newStatus = PurchaseOrderStatus.valueOf(request.status);

        validateStatusTransition(order.id, userId, currentStatus, newStatus);

        // Add to stock when received
        if (newStatus == PurchaseOrderStatus.RECEIVED) {
            addStock(order);
        }

        order.status = newStatus.name();

        LOG.infof("action=UPDATE_PO_STATUS_SUCCESS userId=%s storeId=%s orderId=%s status=%s",
                userId, storeId, orderId, newStatus);

        return purchaseOrderMapper.toResponse(order);
    }

    // Helpers
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PO-" + timestamp + "-" + random;
    }

    private void validateStatusTransition(UUID purchaseOrderId, UUID userId, PurchaseOrderStatus current,
            PurchaseOrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == PurchaseOrderStatus.CONFIRMED || next == PurchaseOrderStatus.CANCELLED;
            case CONFIRMED -> next == PurchaseOrderStatus.RECEIVED || next == PurchaseOrderStatus.CANCELLED;
            case RECEIVED, CANCELLED -> false;
        };

        if (!valid) {
            LOG.warnf(
                    "action=PURCHASE_ORDER_INVALID_STATUS_TRANSITION purchaseOrderId=%s userId=%s currentStatus=%s requestedStatus=%s",
                    purchaseOrderId,
                    userId,
                    current,
                    next);
            throw new BadRequestException("Invalid status transition from " + current + " to " + next);
        }
    }

    private void addStock(PurchaseOrder order) {
        for (PurchaseOrderItem item : order.items) {
            item.product.stockQuantity += item.quantity;
            LOG.debugf("action=STOCK_ADDED productId=%s quantity=%d newStock=%d",
                    item.product.id, item.quantity, item.product.stockQuantity);
        }
    }

    private void validateProductBelongsToStore(Product product, UUID storeId) {
        if (!product.store.id.equals(storeId)) {
            LOG.warnf("action=PRODUCT_STORE_MISMATCH productId=%s productStoreId=%s requestedStoreId=%s",
                    product.id, product.store.id, storeId);
            throw new BadRequestException("Product '" + product.name + "' does not belong to this store");
        }
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);
        if (!canWrite) {
            LOG.warnf("action=PURCHASE_ORDER_WRITE_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("No permission to manage purchase orders");
        }
    }

    private void checkReadPermission(UUID userId, UUID storeId) {
        boolean canRead = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER", "STAFF"), storeId);
        if (!canRead) {
            LOG.warnf("action=PURCHASE_ORDER_READ_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("No permission to view purchase orders");
        }
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

    private PurchaseOrder findPurchaseOrderOrThrow(UUID orderId, UUID storeId) {
        return purchaseOrderRepository.findByIdAndStore(orderId, storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=PURCHASE_ORDER_NOT_FOUND orderId=%s storeId=%s", orderId, storeId);
                    return new NotFoundException("Purchase order not found");
                });
    }
}