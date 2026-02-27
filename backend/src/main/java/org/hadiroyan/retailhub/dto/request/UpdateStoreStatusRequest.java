package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateStoreStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ACTIVE|SUSPEND|CLOSED", message = "Status must be ACTIVE, SUSPEND, or CLOSED")
    public String status;
}
