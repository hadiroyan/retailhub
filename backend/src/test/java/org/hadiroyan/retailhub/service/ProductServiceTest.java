package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateProductRequest;
import org.hadiroyan.retailhub.dto.request.UpdateProductRequest;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.dto.response.ProductResponse;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.mapper.ProductMapper;
import org.hadiroyan.retailhub.model.Product;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.repository.CategoryRepository;
import org.hadiroyan.retailhub.repository.ProductRepository;
import org.hadiroyan.retailhub.repository.StoreRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ProductServiceTest {

    @Inject
    ProductService productService;

    @InjectMock
    ProductRepository productRepository;

    @InjectMock
    CategoryRepository categoryRepository;

    @InjectMock
    StoreRepository storeRepository;

    @InjectMock
    UserRoleRepository userRoleRepository;

    @InjectMock
    ProductMapper productMapper;

    UUID storeId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    Store store;
    Product product;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.id = storeId;

        product = new Product();
        product.id = productId;
        product.sku = "SKU-001";
        product.name = "iPhone 15";
        product.price = BigDecimal.valueOf(15000);
        product.store = store;
    }

    // create product
    @Test
    void should_create_product_success() {

        CreateProductRequest request = new CreateProductRequest();
        request.sku = "SKU-001";
        request.name = "iPhone 15";
        request.price = BigDecimal.valueOf(15000);

        ProductDetailResponse response = new ProductDetailResponse();
        response.sku = "SKU-001";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId))
                .thenReturn(true);

        when(productRepository.existsByStoreAndSku(storeId, "SKU-001"))
                .thenReturn(false);

        when(productMapper.toDetailResponse(any()))
                .thenReturn(response);

        ProductDetailResponse result =
                productService.createProduct(storeId, userId, request);

        assertNotNull(result);
        assertEquals("SKU-001", result.sku);

        verify(productRepository).persist(any(Product.class));
    }

    @Test
    void should_throw_error_when_sku_already_exists() {

        CreateProductRequest request = new CreateProductRequest();
        request.sku = "SKU-001";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId))
                .thenReturn(true);

        when(productRepository.existsByStoreAndSku(storeId, "SKU-001"))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> productService.createProduct(storeId, userId, request));
    }

    // list products
    @Test
    void should_list_products_success() {

        ProductResponse response = new ProductResponse();
        response.sku = "SKU-001";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(productRepository.findByStore(storeId, null, null, null, 0, 10))
                .thenReturn(List.of(product));

        when(productRepository.countByStore(storeId, null, null))
                .thenReturn(1L);

        when(productMapper.toResponse(product))
                .thenReturn(response);

        PagedResponse<ProductResponse> result =
                productService.listProducts(storeId, null, null, null, 0, 10);

        assertEquals(1, result.content.size());
    }

    // get product
    @Test
    void should_get_product_by_sku_success() {

        ProductResponse response = new ProductResponse();
        response.sku = "SKU-001";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(productRepository.findByStoreAndSku(storeId, "SKU-001"))
                .thenReturn(Optional.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(response);

        ProductResponse result =
                productService.getProductBySku(storeId, "SKU-001");

        assertEquals("SKU-001", result.sku);
    }

    // update product
    @Test
    void should_update_product_success() {

        UpdateProductRequest request = new UpdateProductRequest();
        request.name = "iPhone 15 Pro";

        ProductDetailResponse response = new ProductDetailResponse();
        response.name = "iPhone 15 Pro";

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId))
                .thenReturn(true);

        when(productRepository.findByIdOptional(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toDetailResponse(product))
                .thenReturn(response);

        ProductDetailResponse result =
                productService.updateProduct(storeId, productId, userId, request);

        assertNotNull(result);
    }

    // delete product
    @Test
    void should_delete_product_success() {

        when(storeRepository.findByIdOptional(storeId))
                .thenReturn(Optional.of(store));

        when(userRoleRepository.userHasAnyRoleInStore(
                userId, Set.of("OWNER", "ADMIN", "MANAGER"), storeId))
                .thenReturn(true);

        when(productRepository.findByIdOptional(productId))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(storeId, productId, userId);

        verify(productRepository).delete(product);
    }
}