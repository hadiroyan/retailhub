package org.hadiroyan.retailhub.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    public String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    public String description;

    public UUID parentId;

    public String imageUrl;
}
