package org.hadiroyan.retailhub.service;

import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.mapper.ProductMapper;
import org.hadiroyan.retailhub.model.Product;
import org.hadiroyan.retailhub.repository.ProductRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductImageService {

    private static final Logger LOG = Logger.getLogger(ProductImageService.class);

    @Inject
    ProductRepository productRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    FileStorageService fileStorageService;

    @Inject
    ProductMapper productMapper;

    @ConfigProperty(name = "app.upload.max-images-per-product", defaultValue = "5")
    int maxImagesPerProduct;

    // =========================================================================
    // Upload
    // =========================================================================

    @Transactional
    public ProductDetailResponse uploadImage(UUID storeId, UUID productId, UUID userId, FileUpload file) {

        LOG.debugf("action=UPLOAD_PRODUCT_IMAGE_START userId=%s storeId=%s productId=%s",
                userId, storeId, productId);

        validateStoreExists(storeId);
        checkWritePermission(userId, storeId);

        Product product = findProductOrThrow(productId);
        validateSameStoreProduct(product, storeId);

        if (product.imageUrls == null) {
            product.imageUrls = new java.util.ArrayList<>();
        }

        int productImageSize = product.imageUrls.size();
        if (productImageSize >= maxImagesPerProduct) {
            LOG.warnf("action=UPLOAD_IMAGE_DENIED_MAX_SIZE storeId=%s productId=%s imageSize=%d",
                    storeId,
                    productId,
                    productImageSize);
            throw new BadRequestException("Maximum " + maxImagesPerProduct + " images per product");
        }

        String imageUrl = fileStorageService.uploadProductImage(storeId, file);

        product.imageUrls.add(imageUrl);

        LOG.infof("action=UPLOAD_PRODUCT_IMAGE_SUCCESS userId=%s productId=%s imageUrl=%s",
                userId, productId, imageUrl);

        return productMapper.toDetailResponse(product);
    }

    // =========================================================================
    // Delete
    // =========================================================================

    @Transactional
    public ProductDetailResponse deleteImage(UUID storeId, UUID productId,
            UUID userId, String filename) {

        LOG.debugf("action=DELETE_PRODUCT_IMAGE_START userId=%s storeId=%s productId=%s filename=%s",
                userId, storeId, productId, filename);

        validateStoreExists(storeId);
        checkWritePermission(userId, storeId);

        Product product = findProductOrThrow(productId);
        validateSameStoreProduct(product, storeId);
        validateFilename(userId, filename);

        String targetUrl = "products/" + storeId + "/" + filename;
        boolean removed = product.imageUrls.remove(targetUrl);

        if (!removed) {
            LOG.warnf("action=VALIDATE_FAILED_FILENAME_REQUIRED");
            throw new NotFoundException("Image not found");
        }

        fileStorageService.deleteFile(targetUrl);

        LOG.infof("action=DELETE_PRODUCT_IMAGE_SUCCESS userId=%s productId=%s filename=%s",
                userId, productId, filename);

        return productMapper.toDetailResponse(product);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void validateStoreExists(UUID storeId) {
        storeRepository.findByIdOptional(storeId)
                .orElseThrow(() -> {
                    LOG.warnf("action=STORE_NOT_FOUND storeId=%s", storeId);
                    return new NotFoundException("Store not found");
                });
    }

    private void validateFilename(UUID userId, String filename) {
        if (filename == null || filename.isBlank()) {
            LOG.warnf("action=VALIDATE_FAILED_FILENAME_REQUIRED userId=%s", userId);
            throw new BadRequestException("Filename is required");
        }

        if (!filename.matches("^[a-zA-Z0-9\\-\\.]+$")) {
            LOG.warnf("action=VALIDATE_FAILED_FILENAME_INVALID userId=%s filename=%s", userId, filename);
            throw new BadRequestException("Invalid filename");
        }

        if (filename.contains("..")) {
            LOG.warnf("action=VALIDATE_FAILED_FILENAME_INVALID userId=%s filename=%s", userId, filename);
            throw new BadRequestException("Invalid filename");
        }
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
                userId, Set.of("OWNER", "ADMIN", "MANAGER", "STAFF"), storeId);
        if (!canWrite) {
            LOG.warnf("action=UPLOAD_IMAGE_DENIED userId=%s storeId=%s", userId, storeId);
            throw new ForbiddenException("No permission to manage this store's products");
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