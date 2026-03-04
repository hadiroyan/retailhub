package org.hadiroyan.retailhub.repository;

import static org.hadiroyan.retailhub.util.TestConstanst.OWNER_EMAIL;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.Category;
import org.hadiroyan.retailhub.model.Product;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.User;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class ProductRepositoryTest {

    @Inject
    ProductRepository productRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRepository userRepository;

    @Test
    @TestTransaction
    void should_find_product_by_store_and_sku() {
        Store store = createStore();
        Product product = createProduct(store, null, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));

        Optional<Product> result = productRepository.findByStoreAndSku(store.id, product.sku);

        assertTrue(result.isPresent());
        assertEquals("iPhone 15", result.get().name);
    }

    @Test
    @TestTransaction
    void should_return_empty_when_sku_not_found_in_store() {
        Store store = createStore();

        Optional<Product> result = productRepository.findByStoreAndSku(store.id, "SKU-NOT-AVAILABLE");

        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void should_return_true_when_sku_exists_in_store() {
        Store store = createStore();
        Product product = createProduct(store, null, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));

        assertTrue(productRepository.existsByStoreAndSku(store.id, product.sku));
    }

    @Test
    @TestTransaction
    void should_return_false_when_sku_not_exists_in_store() {
        Store store = createStore();

        assertFalse(productRepository.existsByStoreAndSku(store.id, "SKU-NOT-AVAILABLE"));
    }

    @Test
    @TestTransaction
    void should_filter_products_by_name_case_insensitive() {
        Store store = createStore();
        createProduct(store, null, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));
        createProduct(store, null, "SKU-002", "Samsung Galaxy", BigDecimal.valueOf(12000));

        List<Product> result = productRepository.findByStore(store.id, "iphone", null, null, 0, 10);

        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).name);
    }

    @Test
    @TestTransaction
    void should_return_all_products_when_name_filter_is_null() {
        Store store = createStore();
        createProduct(store, null, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));
        createProduct(store, null, "SKU-002", "Samsung Galaxy", BigDecimal.valueOf(12000));

        List<Product> result = productRepository.findByStore(store.id, null, null, null, 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    @TestTransaction
    void should_filter_products_by_category() {
        Store store = createStore();
        Category elektronik = createCategory(store, "Elektronik");
        Category fashion = createCategory(store, "Fashion");

        createProduct(store, elektronik, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));
        createProduct(store, fashion, "SKU-002", "Polo Shirts", BigDecimal.valueOf(100));

        List<Product> result = productRepository.findByStore(store.id, null, elektronik.id, null, 0, 10);

        assertEquals(1, result.size());
        assertEquals("iPhone 15", result.get(0).name);
    }

    @Test
    @TestTransaction
    void should_return_all_products_when_category_filter_is_null() {
        Store store = createStore();
        Category elektronik = createCategory(store, "Elektronik");
        createProduct(store, elektronik, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));
        createProduct(store, null, "SKU-002", "Uncategorized", BigDecimal.valueOf(500));

        List<Product> result = productRepository.findByStore(store.id, null, null, null, 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    @TestTransaction
    void should_sort_products_by_price_ascending() {
        Store store = createStore();
        createProduct(store, null, "SKU-001", "Expensive", BigDecimal.valueOf(20000));
        createProduct(store, null, "SKU-002", "Cheap", BigDecimal.valueOf(5000));
        createProduct(store, null, "SKU-003", "Medium", BigDecimal.valueOf(10000));

        List<Product> result = productRepository.findByStore(store.id, null, null, "asc", 0, 10);

        assertEquals(BigDecimal.valueOf(5000), result.get(0).price);
        assertEquals(BigDecimal.valueOf(10000), result.get(1).price);
        assertEquals(BigDecimal.valueOf(20000), result.get(2).price);
    }

    @Test
    @TestTransaction
    void should_count_all_products_in_store() {
        Store store = createStore();
        createProduct(store, null, "SKU-001", "iPhone 15", BigDecimal.valueOf(15000));
        createProduct(store, null, "SKU-002", "Samsung Galaxy", BigDecimal.valueOf(10000));

        long count = productRepository.countAllByStore(store.id);

        assertEquals(2, count);
    }

    private Store createStore() {
        User owner = userRepository.findByEmail(OWNER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test owner not found"));

        Store store = new Store();
        store.owner = owner;
        store.name = "Test Store";
        store.slug = "test-store-" + UUID.randomUUID();
        store.status = "ACTIVE";
        storeRepository.persist(store);
        return store;
    }

    private Category createCategory(Store store, String name) {
        Category category = new Category();
        category.store = store;
        category.name = name;
        category.slug = name.toLowerCase() + "-" + UUID.randomUUID();
        categoryRepository.persist(category);
        return category;
    }

    private Product createProduct(Store store, Category category,
            String skuPrefix, String name, BigDecimal price) {
        Product product = new Product();
        product.store = store;
        product.category = category;
        product.sku = skuPrefix + "-" + UUID.randomUUID();
        product.name = name;
        product.price = price;
        product.status = "ACTIVE";
        productRepository.persist(product);
        return product;
    }
}