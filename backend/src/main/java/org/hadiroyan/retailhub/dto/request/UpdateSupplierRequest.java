package org.hadiroyan.retailhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 255, message = "Supplier name must not exceed 255 characters")
    public String name;

    @Size(max = 255, message = "Contact person must not exceed 255 characters")
    public String contactPerson;

    @Email(message = "Invalid email format")
    public String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    public String phone;

    public String address;

    public String notes;
}