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
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductService {

    private static final Logger LOG = Logger.getLogger(ProductService.class);

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
    public ProductDetailResponse createProduct(UUID storeId, UUID userId, CreateProductRequest request) {

        LOG.debugf("action=CREATE_PRODUCT_START userId=%s storeId=%s sku=%s name=%s",
                userId, storeId, request.sku, request.name);

        Store store = findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        if (productRepository.existsByStoreAndSku(storeId, request.sku)) {
            LOG.warnf("action=CREATE_PRODUCT_DUPLICATE_SKU userId=%s storeId=%s storeName=%s sku=%s",
                    userId, storeId, store.name, request.sku);
            throw new BadRequestException("SKU already exists");
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

        LOG.infof("action=CREATE_PRODUCT_SUCCESS userId=%s storeId=%s productId=%s sku=%s categoryId=%s",
                userId, storeId, product.id, product.sku,
                category != null ? category.id : "none");

        return productMapper.toDetailResponse(product);
    }

    // Read (Public)
    public PagedResponse<ProductResponse> listProducts(UUID storeId, String name,
            UUID categoryId, String sortByPrice, int page, int size) {

        LOG.debugf("action=LIST_PRODUCTS_START storeId=%s name=%s categoryId=%s page=%d size=%d",
                storeId, name, categoryId, page, size);

        findStoreOrThrow(storeId);

        List<Product> products = productRepository.findByStore(
                storeId, name, categoryId, sortByPrice, page, size);
        long total = productRepository.countByStore(storeId, name, categoryId);

        List<ProductResponse> content = products.stream()
                .map(productMapper::toResponse)
                .toList();

        LOG.infof("action=LIST_PRODUCTS_SUCCESS storeId=%s total=%d page=%d size=%d",
                storeId, total, page, size);

        return new PagedResponse<>(content, page, size, total);
    }

    public ProductResponse getProductBySku(UUID storeId, String sku) {

        LOG.debugf("action=GET_PRODUCT_BY_SKU_START storeId=%s sku=%s", storeId, sku);

        findStoreOrThrow(storeId);

        Product product = productRepository.findByStoreAndSku(storeId, sku)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        LOG.infof("action=GET_PRODUCT_BY_SKU_SUCCESS storeId=%s sku=%s productId=%s",
                storeId, sku, product.id);

        return productMapper.toResponse(product);
    }

    // Read (Internal for OWNER, ADMIN, MANAGER)
    public ProductDetailResponse getProductDetail(UUID storeId, String sku, UUID userId) {

        LOG.debugf("action=GET_PRODUCT_DETAIL_START userId=%s storeId=%s sku=%s",
                userId, storeId, sku);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Product product = productRepository.findByStoreAndSku(storeId, sku)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        LOG.infof("action=GET_PRODUCT_DETAIL_SUCCESS userId=%s storeId=%s sku=%s productId=%s",
                userId, storeId, sku, product.id);

        return productMapper.toDetailResponse(product);
    }

    // Update
    @Transactional
    public ProductDetailResponse updateProduct(UUID storeId, UUID productId,
            UUID userId, UpdateProductRequest request) {

        LOG.debugf("action=UPDATE_PRODUCT_START userId=%s storeId=%s productId=%s",
                userId, storeId, productId);

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

        LOG.infof("action=UPDATE_PRODUCT_SUCCESS userId=%s storeId=%s productId=%s sku=%s categoryId=%s",
                userId, storeId, product.id, product.sku,
                category != null ? category.id : "none");

        return productMapper.toDetailResponse(product);
    }

    // Delete
    @Transactional
    public void deleteProduct(UUID storeId, UUID productId, UUID userId) {

        LOG.debugf("action=DELETE_PRODUCT_START userId=%s storeId=%s productId=%s",
                userId, storeId, productId);

        findStoreOrThrow(storeId);
        checkWritePermission(userId, storeId);

        Product product = findProductOrThrow(productId);
        validateSameStoreProduct(product, storeId);

        productRepository.delete(product);

        LOG.infof("action=DELETE_PRODUCT_SUCCESS userId=%s storeId=%s productId=%s sku=%s",
                userId, storeId, productId, product.sku);
    }

    // helper ------------------------------------------------------------------
    private Store findStoreOrThrow(UUID storeId) {
        return storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });
    }

    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findByIdOptional(categoryId)
                .orElseThrow(() -> {
                    LOG.warnf("action=CATEGORY_NOT_FOUND categoryId=%s", categoryId);
                    return new NotFoundException("Category not found");
                });
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findByIdOptional(productId)
                .orElseThrow(() -> {
                    LOG.warnf("action=PRODUCT_NOT_FOUND productId=%s", productId);
                    return new NotFoundException("Product not found");
                });
    }

    private void checkWritePermission(UUID userId, UUID storeId) {
        boolean canWrite = userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId);

        if (!canWrite) {
            LOG.warnf("action=WRITE_PERMISSION_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("You don't have permission to manage this store's products");
        }
    }

    private void validateSameStore(Category category, UUID storeId) {
        if (!category.store.id.equals(storeId)) {
            LOG.warnf("action=CATEGORY_STORE_MISMATCH categoryId=%s categoryStoreId=%s requestedStoreId=%s",
                    category.id, category.store.id, storeId);
            throw new BadRequestException("Category does not belong to this store");
        }
    }

    private void validateSameStoreProduct(Product product, UUID storeId) {
        if (!product.store.id.equals(storeId)) {
            LOG.warnf("action=PRODUCT_STORE_MISMATCH productId=%s productStoreId=%s requestedStoreId=%s",
                    product.id, product.store.id, storeId);
            throw new BadRequestException("Product does not belong to this store");
        }
    }
}
