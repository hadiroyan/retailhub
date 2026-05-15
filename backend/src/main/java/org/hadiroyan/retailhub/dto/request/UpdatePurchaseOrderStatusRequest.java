package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdatePurchaseOrderStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|CONFIRMED|RECEIVED|CANCELLED", message = "Status must be PENDING, CONFIRMED, RECEIVED, or CANCELLED")
    public String status;
}