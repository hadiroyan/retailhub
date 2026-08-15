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
        checkImagePermission(userId, storeId);

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
            UUID userId, String publicId) {

        LOG.debugf("action=DELETE_PRODUCT_IMAGE_START userId=%s storeId=%s productId=%s publicId=%s",
                userId, storeId, productId, publicId);

        validateStoreExists(storeId);
        checkImagePermission(userId, storeId);

        Product product = findProductOrThrow(productId);
        validateSameStoreProduct(product, storeId);
        validatePublicId(userId, publicId);

        boolean removed = product.imageUrls.remove(publicId);

        if (!removed) {
            LOG.warnf("action=IMAGE_NOT_FOUND_IN_PRODUCT productId=%s publicId=%s", productId, publicId);
            throw new NotFoundException("Image not found");
        }

        fileStorageService.deleteFile(publicId);

        LOG.infof("action=DELETE_PRODUCT_IMAGE_SUCCESS userId=%s productId=%s publicId=%s",
                userId, productId, publicId);

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

    private void validatePublicId(UUID userId, String publicId) {
        if (publicId == null || publicId.isBlank()) {
            LOG.warnf("action=VALIDATE_FAILED_PUBLIC_ID_REQUIRED userId=%s", userId);
            throw new BadRequestException("Image identifier is required");
        }

        if (!publicId.matches("^[a-zA-Z0-9/\\-]+$")) {
            LOG.warnf("action=VALIDATE_FAILED_PUBLIC_ID_INVALID userId=%s publicId=%s", userId, publicId);
            throw new BadRequestException("Invalid image identifier");
        }

        if (publicId.contains("..")) {
            LOG.warnf("action=VALIDATE_FAILED_PUBLIC_ID_INVALID userId=%s publicId=%s", userId, publicId);
            throw new BadRequestException("Invalid image identifier");
        }
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findByIdOptional(productId)
                .orElseThrow(() -> {
                    LOG.warnf("action=PRODUCT_NOT_FOUND productId=%s", productId);
                    return new NotFoundException("Product not found");
                });
    }

    private void checkImagePermission(UUID userId, UUID storeId) {
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