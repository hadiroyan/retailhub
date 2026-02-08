package org.hadiroyan.retailhub.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateEmployeeRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    public String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    public String password;

    @NotBlank(message = "Full name is required")
    public String fullName;
    
    @NotBlank(message = "Role is required")
    public String role;  
    
    @NotNull(message = "Store ID is required")
    public UUID storeId; 
    
    public CreateEmployeeRequest() {}
}