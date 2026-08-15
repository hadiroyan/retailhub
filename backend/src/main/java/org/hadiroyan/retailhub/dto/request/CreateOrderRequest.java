package org.hadiroyan.retailhub.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOrderRequest {

    @NotNull(message = "Store ID is required")
    public UUID storeId;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must not exceed 100 characters")
    public String recipientName;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    public String phone;

    @NotBlank(message = "Shipping address is required")
    public String shippingAddress;

    public String notes;

    @NotEmpty(message = "Order must have at least one item")
    public List<@Valid CreateOrderItemRequest> items;
}