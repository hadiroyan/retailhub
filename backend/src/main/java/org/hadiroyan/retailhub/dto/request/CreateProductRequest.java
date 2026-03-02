package org.hadiroyan.retailhub.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    public String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    public String name;

    public String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    public BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cost price must be greater than 0")
    public BigDecimal costPrice;

    @Min(value = 0, message = "Stock quantity must be 0 or greater")
    public Integer stockQuantity = 0;

    @Min(value = 0, message = "Min stock level must be 0 or greater")
    public Integer minStockLevel = 10;

    @Pattern(regexp = "ACTIVE|DRAFT|OUT_OF_STOCK", message = "Status must be ACTIVE, DRAFT, or OUT_OF_STOCK")
    public String status = "ACTIVE";

    public UUID categoryId;

    public String imageUrls;
}