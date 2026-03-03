package org.hadiroyan.retailhub.repository;

import static org.hadiroyan.retailhub.util.TestConstanst.OWNER_EMAIL;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.Category;
import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.User;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class CategoryRepositoryTest {

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRepository userRepository;

    // Find by store and slug
    @Test
    @TestTransaction
    void should_find_category_by_store_and_slug() {
        Store store = createStore();
        Category category = createCategory(store, "Elektronik", "elektronik", null);

        Optional<Category> result = categoryRepository.findByStoreAndSlug(store.id, category.slug);

        assertTrue(result.isPresent());
        assertEquals("Elektronik", result.get().name);
    }

    @Test
    @TestTransaction
    void should_return_empty_when_slug_not_found_in_store() {
        Store store = createStore();

        Optional<Category> result = categoryRepository.findByStoreAndSlug(store.id, "tidak-ada");
        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void should_not_find_category_slug_from_different_store() {
        Store storeA = createStore();
        Store storeB = createStore();
        Category category = createCategory(storeA, "Elektronik", "elektronik", null);

        // slug ada di storeA, dicari di storeB
        Optional<Category> result = categoryRepository.findByStoreAndSlug(storeB.id, category.slug);
        assertTrue(result.isEmpty());
    }

    // Exists by store and slug
    @Test
    @TestTransaction
    void should_return_true_when_slug_exists_in_store() {
        Store store = createStore();
        Category category = createCategory(store, "Elektronik", "elektronik", null);

        assertTrue(categoryRepository.existsByStoreAndSlug(store.id, category.slug));
    }

    @Test
    @TestTransaction
    void should_return_false_when_slug_not_exists_in_store() {
        Store store = createStore();

        assertFalse(categoryRepository.existsByStoreAndSlug(store.id, "tidak-ada"));
    }

    @Test
    @TestTransaction
    void should_return_false_when_slug_belongs_to_same_category() {
        Store store = createStore();
        Category category = createCategory(store, "Elektronik", "elektronik", null);

        assertFalse(categoryRepository
                .existsByStoreAndSlugAndIdNot(store.id, category.slug, category.id));
    }

    @Test
    @TestTransaction
    void should_return_true_when_slug_taken_by_another_category() {
        Store store = createStore();
        Category catA = createCategory(store, "Elektronik", "elektronik", null);
        Category catB = createCategory(store, "Fashion", "fashion", null);

        assertTrue(categoryRepository
                .existsByStoreAndSlugAndIdNot(store.id, catA.slug, catB.id));
    }

    // Find root category by store
    @Test
    @TestTransaction
    void should_return_only_root_categories() {
        Store store = createStore();
        Category parent = createCategory(store, "Elektronik", "elektronik", null);
        createCategory(store, "Handphone", "handphone", parent); // child

        List<Category> roots = categoryRepository.findRootsByStore(store.id);

        assertEquals(1, roots.size());
        assertEquals("Elektronik", roots.get(0).name);
    }

    @Test
    @TestTransaction
    void should_return_empty_when_store_has_no_categories() {
        Store store = createStore();

        assertTrue(categoryRepository.findRootsByStore(store.id).isEmpty());
    }

    // Find child category by parent
    @Test
    @TestTransaction
    void should_return_children_of_parent_category() {
        Store store = createStore();
        Category parent = createCategory(store, "Elektronik", "elektronik", null);
        createCategory(store, "Handphone", "handphone", parent);
        createCategory(store, "Laptop", "laptop", parent);

        List<Category> children = categoryRepository.findChildrenByParent(parent.id);

        assertEquals(2, children.size());
    }

    @Test
    @TestTransaction
    void should_return_empty_when_category_has_no_children() {
        Store store = createStore();
        Category category = createCategory(store, "Elektronik", "elektronik", null);

        assertTrue(categoryRepository.findChildrenByParent(category.id).isEmpty());
    }

    // Find by store and count by store
    @Test
    @TestTransaction
    void should_return_all_categories_including_children() {
        Store store = createStore();
        Category parent = createCategory(store, "Elektronik", "elektronik", null);
        createCategory(store, "Handphone", "handphone", parent);

        List<Category> all = categoryRepository.findAllByStore(store.id, 0, 10);

        assertEquals(2, all.size());
    }

    @Test
    @TestTransaction
    void should_count_all_categories_in_store() {
        Store store = createStore();
        Category parent = createCategory(store, "Elektronik", "elektronik", null);
        createCategory(store, "Handphone", "handphone", parent);

        assertEquals(2, categoryRepository.countByStore(store.id));
    }

    @Test
    @TestTransaction
    void should_not_mix_categories_between_stores() {
        Store storeA = createStore();
        Store storeB = createStore();
        createCategory(storeA, "Elektronik", "elektronik", null);
        createCategory(storeB, "Fashion", "fashion", null);

        assertEquals(1, categoryRepository.countByStore(storeA.id));
        assertEquals(1, categoryRepository.countByStore(storeB.id));
    }

    @Test
    @TestTransaction
    void should_respect_page_size() {
        Store store = createStore();
        createCategory(store, "Cat A", "cat-a", null);
        createCategory(store, "Cat B", "cat-b", null);
        createCategory(store, "Cat C", "cat-c", null);

        List<Category> page = categoryRepository.findAllByStore(store.id, 0, 2);

        assertTrue(page.size() <= 2);
    }

    // Count product
    @Test
    @TestTransaction
    void should_return_zero_when_category_has_no_products() {
        Store store = createStore();
        Category category = createCategory(store, "Elektronik", "elektronik", null);

        assertEquals(0, categoryRepository.countProductsByCategory(category.id));
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

    private Category createCategory(Store store, String name, String slugPrefix, Category parent) {
        Category category = new Category();
        category.store = store;
        category.name = name;
        category.slug = slugPrefix + "-" + UUID.randomUUID(); // unique per test run
        category.parent = parent;
        categoryRepository.persist(category);
        return category;
    }

}