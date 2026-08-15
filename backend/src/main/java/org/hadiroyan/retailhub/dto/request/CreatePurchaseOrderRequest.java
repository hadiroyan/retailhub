package org.hadiroyan.retailhub.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreatePurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    public UUID supplierId;

    public LocalDate expectedDeliveryDate;

    public String notes;

    @NotEmpty(message = "Purchase order must have at least one item")
    public List<@Valid CreatePurchaseOrderItemRequest> items;
}