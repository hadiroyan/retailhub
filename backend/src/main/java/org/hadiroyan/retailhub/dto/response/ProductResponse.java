package org.hadiroyan.retailhub.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    public UUID id;
    public String sku;
    public String name;
    public String description;
    public BigDecimal price;
    public Integer stockQuantity;
    public String status;
    public String imageUrls;

    public CategoryInfo category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryInfo {
        public UUID id;
        public String name;
        public String slug;
    }
}