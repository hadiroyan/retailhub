package org.hadiroyan.retailhub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseOrderResponse {

    public UUID id;
    public String orderNumber;
    public String status;
    public BigDecimal totalAmount;
    public LocalDate expectedDeliveryDate;
    public String notes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public SupplierInfo supplier;
    public List<PurchaseOrderItemResponse> items;

    public static class SupplierInfo {
        public UUID id;
        public String name;
        public String contactPerson;
        public String phone;
    }
}