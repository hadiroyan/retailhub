package org.hadiroyan.retailhub.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateProductRequest;
import org.hadiroyan.retailhub.dto.request.UpdateProductRequest;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.dto.response.ProductResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.ForbiddenException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.ProductMapper;
import org.hadiroyan.retailhub.model.Category;
import org.hadiroyan.retailhub.model.Product;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.repository.CategoryRepository;
import org.hadiroyan.retailhub.repository.ProductRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductService {

    @Inject
    StoreRepository storeRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    ProductMapper productMapper;

    // Create
    @Transactional
    public ProductDetailResponse createProduct(UUID storeId, UUID userId,
            CreateProductRequest request) {

        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        // SKU check
        if (productRepository.existsByStoreAndSku(storeId, request.sku)) {
            throw new BadRequestException("SKU '" + request.sku + "' already exists in this store");
        }

        Category category = null;
        if (request.categoryId != null) {
            category = findCategoryOrThrow(request.categoryId);
            validateSameStore(category, storeId);
        }

        Product product = new Product();
        product.store = store;
        product.category = category;
        product.sku = request.sku;
        product.name = request.name;
        product.description = request.description;
        product.price = request.price;
        product.costPrice = request.costPrice;
        product.stockQuantity = request.stockQuantity != null ? request.stockQuantity : 0;
        product.minStockLevel = request.minStockLevel != null ? request.minStockLevel : 10;
        product.status = request.status != null ? request.status : "ACTIVE";
        product.imageUrls = request.imageUrls;

        productRepository.persist(product);

        return productMapper.toDetailResponse(product);
    }

    // Read (Public)
    public PagedResponse<ProductResponse> listProducts(UUID storeId, String name,
            UUID categoryId, String sortByPrice, int page, int size) {

        findStoreOrThrow(storeId);

        List<Product> products = productRepository.findByStore(
                storeId, name, categoryId, sortByPrice, page, size);
        long total = productRepository.countByStore(storeId, name, categoryId);

        List<ProductResponse> content = products.stream()
                .map(productMapper::toResponse)
                .toList();

        return new PagedResponse<>(content, page, size, total);
    }

    public ProductResponse getProductBySku(UUID storeId, String sku) {
        findStoreOrThrow(storeId);

        Product product = productRepository.findByStoreAndSku(storeId, sku)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return productMapper.toResponse(product);
    }

    // Read (Internal for OWNER, ADMIN, MANAGER)
    public ProductDetailResponse getProductDetail(UUID storeId, String sku, UUID userId) {
        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Product product = productRepository.findByStoreAndSku(storeId, sku)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return productMapper.toDetailResponse(product);
    }

    // Update
    @Transactional
    public ProductDetailResponse updateProduct(UUID storeId, UUID productId,
            UUID userId, UpdateProductRequest request) {

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Product product = findProductOrThrow(productId);
        validateSameStoreProduct(product, storeId);

        Category category = null;
        if (request.categoryId != null) {
            category = findCategoryOrThrow(request.categoryId);
            validateSameStore(category, storeId);
        }

        product.name = request.name;
        product.description = request.description;
        product.price = request.price;
        product.costPrice = request.costPrice;
        product.stockQuantity = request.stockQuantity;
        product.minStockLevel = request.minStockLevel;
        product.status = request.status;
        product.imageUrls = request.imageUrls;
        product.category = category;

        return productMapper.toDetailResponse(product);
    }

    // Delete
    @Transactional
    public void deleteProduct(UUID storeId, UUID productId, UUID userId) {
        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Product product = findProductOrThrow(productId);
        validateSameStoreProduct(product, storeId);

        productRepository.delete(product);
    }

    // helper ------------------------------------------------------------------
    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> new NotFoundException("Store not found"));
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findByIdOptional(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);
        if (!canWrite) {
            throw new ForbiddenException("You don't have permission to manage this store's products");
        }
    }

    private void validateSameStore(Category category, UUID storeId) {
        if (!category.store.id.equals(storeId)) {
            throw new BadRequestException("Category does not belong to this store");
        }
    }

    private void validateSameStoreProduct(Product product, UUID storeId) {
        if (!product.store.id.equals(storeId)) {
            throw new BadRequestException("Product does not belong to this store");
        }
    }
}
