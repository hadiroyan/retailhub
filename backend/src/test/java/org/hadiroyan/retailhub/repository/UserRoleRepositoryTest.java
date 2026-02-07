package org.hadiroyan.retailhub.repository;

import static org.hadiroyan.retailhub.util.TestConstanst.SUPER_ADMIN_ID;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.model.UserRole;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class UserRoleRepositoryTest {

    @Inject
    UserRoleRepository userRoleRepository;

    @Test
    void should_find_user_roles_by_user_id() {
        List<UserRole> userRoles = userRoleRepository.findByUserId(SUPER_ADMIN_ID);

        assertEquals(1, userRoles.size());
    }

    @Test
    void should_find_user_roles_with_roles_and_privileges_loaded() {
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRoles(SUPER_ADMIN_ID);

        assertFalse(userRoles.isEmpty());

        assertDoesNotThrow(() -> {
            userRoles.forEach(ur -> {
                assertNotNull(ur.role);
                assertEquals("SUPER_ADMIN", ur.role.name);
                assertFalse(ur.role.privileges.isEmpty(), "SUPER_ADMIN should have privileges");
            });
        });
    }

    @Test
    void should_find_global_roles() {
        List<UserRole> globalRoles = userRoleRepository.findGlobalRolesByUserId(SUPER_ADMIN_ID);

        assertEquals(1, globalRoles.size());
        assertNull(globalRoles.get(0).storeId, "Global role should have NULL storeId");
    }

    @Test
    void should_check_user_has_global_role() {
        boolean hasSuperAdmin = userRoleRepository.userHasGlobalRole(SUPER_ADMIN_ID, "SUPER_ADMIN");
        boolean hasOwner = userRoleRepository.userHasGlobalRole(SUPER_ADMIN_ID, "OWNER");

        assertTrue(hasSuperAdmin);
        assertFalse(hasOwner);
    }

    @Test
    void should_return_empty_for_store_roles_when_no_store_assigned() {
        UUID randomStoreId = UUID.randomUUID();

        List<UserRole> storeRoles = userRoleRepository
                .findStoreRolesByUserId(SUPER_ADMIN_ID, randomStoreId);

        assertTrue(storeRoles.isEmpty(), "User should not have store-specific roles yet");
    }

    @Test
    void should_not_have_role_in_non_existent_store() {
        UUID randomStoreId = UUID.randomUUID();

        boolean hasRole = userRoleRepository
                .userHasRoleInStore(SUPER_ADMIN_ID, "SUPER_ADMIN", randomStoreId);

        assertFalse(hasRole, "SUPER_ADMIN is global, not store-specific");
    }

    @TestTransaction
    void should_delete_user_roles_by_user_id() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertDoesNotThrow(() -> {
            long deleted = userRoleRepository.deleteByUserId(nonExistentUserId);
            assertEquals(0, deleted);
        });
    }
}