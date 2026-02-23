package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateStoreRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 255, message = "Store name must not exceed 255 characters")
    public String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    public String description;

    public String address;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    public String phone;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    public String email;
}
