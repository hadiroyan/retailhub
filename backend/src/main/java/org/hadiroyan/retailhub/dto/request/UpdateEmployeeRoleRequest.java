package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateEmployeeRoleRequest {

    // ADMIN, MANAGER, STAFF
    @NotBlank(message = "Role is requeired")
    public String role;
}
