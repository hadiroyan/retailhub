package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|PROCESSING|SHIPPED|DELIVERED|CANCELLED", message = "Status must be PENDING, PROCESSING, SHIPPED, DELIVERED, or CANCELLED")
    public String status;

    // Optional — untuk SHIPPED status
    public String trackingNumber;
}