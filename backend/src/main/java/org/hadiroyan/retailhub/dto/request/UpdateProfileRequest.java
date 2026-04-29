package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    public String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    public String phone;

    public String address;

}
