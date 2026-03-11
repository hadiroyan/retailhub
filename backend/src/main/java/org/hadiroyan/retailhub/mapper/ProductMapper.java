package org.hadiroyan.retailhub.mapper;

import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.dto.response.ProductResponse;
import org.hadiroyan.retailhub.model.Product;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.id;
        response.sku = product.sku;
        response.name = product.name;
        response.description = product.description;
        response.price = product.price;
        response.stockQuantity = product.stockQuantity;
        response.status = product.status;
        response.imageUrls = product.imageUrls;

        if (product.category != null) {
            ProductResponse.CategoryInfo categoryInfo = new ProductResponse.CategoryInfo();
            categoryInfo.id = product.category.id;
            categoryInfo.name = product.category.name;
            categoryInfo.slug = product.category.slug;
            response.category = categoryInfo;
        }

        return response;
    }

    // Internal response — include costPrice dan minStockLevel
    public ProductDetailResponse toDetailResponse(Product product) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.id = product.id;
        response.sku = product.sku;
        response.name = product.name;
        response.description = product.description;
        response.price = product.price;
        response.costPrice = product.costPrice;
        response.stockQuantity = product.stockQuantity;
        response.minStockLevel = product.minStockLevel;
        response.status = product.status;
        response.imageUrls = product.imageUrls;
        response.createdAt = product.createdAt;
        response.updatedAt = product.updatedAt;

        if (product.category != null) {
            ProductDetailResponse.CategoryInfo categoryInfo = new ProductDetailResponse.CategoryInfo();
            categoryInfo.id = product.category.id;
            categoryInfo.name = product.category.name;
            categoryInfo.slug = product.category.slug;
            response.category = categoryInfo;
        }

        return response;
    }
}
