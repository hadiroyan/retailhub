package org.hadiroyan.retailhub.utils;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import org.hadiroyan.retailhub.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("CurrentUserUtil Test")
class CurrentUserUtilTest {

    @Inject
    CurrentUserUtil currentUser;

    static final String USER_ID_STR = "11111111-1111-1111-1111-111111111111";
    static final UUID USER_ID = UUID.fromString(USER_ID_STR);
    static final UUID STORE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    // =====================================================
    // getUserId()
    // =====================================================

    @Test
    @TestSecurity(user = USER_ID_STR, roles = "OWNER")
    void getUserId_shouldReturnUUID_whenAuthenticated() {
        UUID result = currentUser.getUserId();
        assertEquals(USER_ID, result);
    }

    @Test
    void getUserId_shouldThrowException_whenAnonymous() {
        assertThrows(UnauthorizedException.class, () -> {
            currentUser.getUserId();
        });
    }

    // =====================================================
    // getUserIdOptional()
    // =====================================================

    @Test
    @TestSecurity(user = USER_ID_STR, roles = "OWNER")
    void getUserIdOptional_shouldReturnValue_whenAuthenticated() {
        Optional<UUID> result = currentUser.getUserIdOptional();

        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get());
    }

    @Test
    void getUserIdOptional_shouldReturnEmpty_whenAnonymous() {
        Optional<UUID> result = currentUser.getUserIdOptional();

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // getEmail()
    // =====================================================

    @Test
    @TestSecurity(user = USER_ID_STR, roles = "OWNER")
    @JwtSecurity(claims = {
            @Claim(key = "upn", value = "owner@example.com")
    })
    void getEmail_shouldReturnEmail_whenAuthenticated() {
        String email = currentUser.getEmail();
        assertEquals("owner@example.com", email);
    }

    // =====================================================
    // getRoles()
    // =====================================================

    @Test
    @TestSecurity(user = USER_ID_STR, roles = { "OWNER", "CUSTOMER" })
    void getRoles_shouldReturnRoles_whenAuthenticated() {
        Set<String> roles = currentUser.getRoles();

        assertTrue(roles.contains("OWNER"));
        assertTrue(roles.contains("CUSTOMER"));
    }

    // =====================================================
    // hasRole()
    // =====================================================

    @Test
    @TestSecurity(user = USER_ID_STR, roles = "OWNER")
    void hasRole_shouldReturnTrue_whenUserHasRole() {
        assertTrue(currentUser.hasRole("OWNER"));
        assertFalse(currentUser.hasRole("SUPER_ADMIN"));
    }

    // =====================================================
    // hasStoreAccess() (simple version)
    // =====================================================

    @Test
    @TestSecurity(user = USER_ID_STR, roles = "SUPER_ADMIN")
    void hasStoreAccess_shouldReturnTrue_forSuperAdmin() {
        assertTrue(currentUser.hasStoreAccess(STORE_ID));
    }

    @Test
    void hasStoreAccess_shouldReturnFalse_forAnonymous() {
        assertFalse(currentUser.hasStoreAccess(STORE_ID));
    }
}