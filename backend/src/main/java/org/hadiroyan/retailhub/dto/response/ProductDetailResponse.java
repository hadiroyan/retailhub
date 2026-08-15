package org.hadiroyan.retailhub.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {

    public UUID id;
    public String sku;
    public String name;
    public String description;
    public BigDecimal price;

    // Internal only
    public BigDecimal costPrice;

    public Integer stockQuantity;
    public Integer minStockLevel;
    public String status;
    public List<ImageInfo> imageUrls;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public CategoryInfo category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryInfo {
        public UUID id;
        public String name;
        public String slug;
    }

    public static class ImageInfo {
        public String publicId;
        public String url;
    }
}