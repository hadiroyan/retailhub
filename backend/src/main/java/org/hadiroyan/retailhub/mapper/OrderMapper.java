package org.hadiroyan.retailhub.mapper;

import org.hadiroyan.retailhub.dto.response.OrderItemResponse;
import org.hadiroyan.retailhub.dto.response.OrderResponse;
import org.hadiroyan.retailhub.model.SalesOrder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderMapper {

    public OrderResponse toResponse(SalesOrder order) {
        OrderResponse response = new OrderResponse();
        response.id = order.id;
        response.orderNumber = order.orderNumber;
        response.status = order.status;
        response.totalAmount = order.totalAmount;
        response.recipientName = order.recipientName;
        response.phone = order.phone;
        response.shippingAddress = order.shippingAddress;
        response.trackingNumber = order.trackingNumber;
        response.notes = order.notes;
        response.createdAt = order.createdAt;
        response.updatedAt = order.updatedAt;

        // Store info
        OrderResponse.StoreInfo storeInfo = new OrderResponse.StoreInfo();
        storeInfo.id = order.store.id;
        storeInfo.name = order.store.name;
        storeInfo.slug = order.store.slug;
        response.store = storeInfo;

        // Items
        response.items = order.items.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.id = item.id;
            itemResponse.productId = item.product.id;
            itemResponse.sku = item.product.sku;
            itemResponse.name = item.product.name;
            itemResponse.imageUrl = item.product.imageUrls != null && !item.product.imageUrls.isEmpty()
                    ? item.product.imageUrls.get(0)
                    : null;
            itemResponse.quantity = item.quantity;
            itemResponse.unitPrice = item.unitPrice;
            itemResponse.subtotal = item.subtotal;
            return itemResponse;
        }).toList();

        return response;
    }
}
