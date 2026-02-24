package org.hadiroyan.retailhub.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.model.Store;
import org.hadiroyan.retailhub.model.StoreStatus;
import org.hadiroyan.retailhub.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
public class StoreRepositoryTest {

    @Inject
    StoreRepository storeRepository;

    @Inject
    UserRepository userRepository;

    UUID ownerId;

    @BeforeAll
    @Transactional
    void setupStores() {
        Optional<User> optOwner = userRepository.findByEmail("test.owner@test.com");
        assertTrue(optOwner.isPresent());

        User owner = optOwner.get();
        ownerId = owner.id;

        Store activeStore = new Store();
        activeStore.owner = owner;
        activeStore.name = "Toko Aktif";
        activeStore.slug = "toko-aktif";
        activeStore.status = StoreStatus.ACTIVE.name();
        storeRepository.persist(activeStore);

        Store suspendedStore = new Store();
        suspendedStore.owner = owner;
        suspendedStore.name = "Toko Suspend";
        suspendedStore.slug = "toko-suspend";
        suspendedStore.status = StoreStatus.SUSPEND.name();
        storeRepository.persist(suspendedStore);
    }

    // findBySlug
    @Test
    void should_return_store_when_slug_exists() {
        Optional<Store> result = storeRepository.findBySlug("toko-aktif");
        assertTrue(result.isPresent(), "store must be present");
        assertEquals(result.get().slug, "toko-aktif");
        assertEquals(result.get().name, "Toko Aktif");
    }

    @Test
    void should_return_empty_when_slug_does_not_exist() {
        Optional<Store> result = storeRepository.findBySlug("unknown-store");
        assertFalse(result.isPresent(), "Should not return store with unknown slug");
    }

    // existsBySlut
    @Test
    void should_return_true_when_slug_exists() {
        assertTrue(storeRepository.existsBySlug("toko-aktif"));
    }

    @Test
    void should_return_false_when_slug_does_not_exist() {
        assertFalse(storeRepository.existsBySlug("unknown-store"));
    }

    // findAllActive
    @Test
    void should_return_only_active_stores() {
        List<Store> results = storeRepository.findAllActive(0, 1);

        assertEquals(1, results.size());
        assertEquals(StoreStatus.ACTIVE.name(), results.get(0).status);
    }

    // findByOwner
    @Test
    void should_return_store_of_owner() {
        List<Store> result = storeRepository.findByOwner(ownerId, 0, 10);
        assertEquals(2, result.size());
    }

    // Count
    @Test
    void should_return_correct_total_store() {
        long result = storeRepository.countAll();
        assertEquals(2, result);
    }

    @Test
    void should_return_total_active_store() {
        long result = storeRepository.countAllActive();
        assertEquals(1, result);
    }
}
