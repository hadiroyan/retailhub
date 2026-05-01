package org.hadiroyan.retailhub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    public UUID id;
    public String orderNumber;
    public String status;
    public BigDecimal totalAmount;
    public String recipientName;
    public String phone;
    public String shippingAddress;
    public String trackingNumber;
    public String notes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public StoreInfo store;
    public List<OrderItemResponse> items;

    public static class StoreInfo {
        public UUID id;
        public String name;
        public String slug;
    }
}