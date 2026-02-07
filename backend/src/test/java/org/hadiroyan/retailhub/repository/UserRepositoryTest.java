package org.hadiroyan.retailhub.repository;

import static org.hadiroyan.retailhub.util.TestConstanst.CUSTOMER_EMAIL;
import static org.hadiroyan.retailhub.util.TestConstanst.DISABLED_USER_ID;
import static org.hadiroyan.retailhub.util.TestConstanst.OAUTH_USER_ID;
import static org.hadiroyan.retailhub.util.TestConstanst.SUPER_ADMIN_EMAIL;
import static org.hadiroyan.retailhub.util.TestConstanst.SUPER_ADMIN_ID;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.hadiroyan.retailhub.model.User;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Test
    void should_find_test_super_admin_by_email() {
        Optional<User> optUser = userRepository.findByEmail(SUPER_ADMIN_EMAIL);

        assertTrue(optUser.isPresent());
        assertEquals(SUPER_ADMIN_ID, optUser.get().id);
        assertEquals(SUPER_ADMIN_EMAIL, optUser.get().email);
        assertTrue(optUser.get().enabled);
        assertTrue(optUser.get().emailVerified);
    }

    @Test
    void should_find_oauth_user_by_provider() {
        Optional<User> optUser = userRepository
                .findByProviderAndProviderId("GOOGLE", "google-id-12345");

        assertTrue(optUser.isPresent());
        assertEquals(OAUTH_USER_ID, optUser.get().id);
        assertEquals("GOOGLE", optUser.get().provider);
    }

    @Test
    void should_find_user_with_roles_loaded() {
        Optional<User> optUser = userRepository.findByEmailWithRoles(SUPER_ADMIN_EMAIL);

        assertTrue(optUser.isPresent());

        User user = optUser.get();
        assertFalse(user.userRoles.isEmpty());

        assertDoesNotThrow(() -> {
            user.userRoles.forEach(ur -> {
                assertNotNull(ur.role);
                assertEquals("SUPER_ADMIN", ur.role.name);
            });
        });
    }

    @Test
    void should_check_email_exists() {
        // When & Then
        assertTrue(userRepository.existsByEmail(SUPER_ADMIN_EMAIL));
        assertTrue(userRepository.existsByEmail(CUSTOMER_EMAIL));
        assertFalse(userRepository.existsByEmail("custom.email@test.com"));
    }

    @Test
    void should_find_only_enabled_users() {
        List<User> enabledUsers = userRepository.findEnabledUsers();

        assertEquals(4, enabledUsers.size(), "Should have 4 enabled users (excluding disabled)");
        enabledUsers.forEach(user -> assertTrue(user.enabled));

        enabledUsers.forEach(user -> assertNotEquals(DISABLED_USER_ID, user.id));
    }

    @Test
    void should_find_users_by_role_name() {
        List<User> superAdmins = userRepository.findByRoleName("SUPER_ADMIN");
        List<User> customers = userRepository.findByRoleName("CUSTOMER");

        assertEquals(1, superAdmins.size());
        assertEquals(3, customers.size(), "3 CUSTOMER users (including disabled and OAuth)");
    }

    @Test
    void should_count_users_by_role() {
        long superAdminCount = userRepository.countByRole("SUPER_ADMIN");
        long customerCount = userRepository.countByRole("CUSTOMER");
        long ownerCount = userRepository.countByRole("OWNER");

        assertEquals(1, superAdminCount);
        assertEquals(3, customerCount);
        assertEquals(1, ownerCount);
    }

    @Test
    void should_return_zero_for_non_existent_role() {
        long count = userRepository.countByRole("USER");

        assertEquals(0, count);
    }
}