package org.hadiroyan.retailhub.service;

import static org.hadiroyan.retailhub.util.TestConstanst.SUPER_ADMIN_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.repository.RoleRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.transaction.Transactional;

@QuarkusTest
public class JwtTokenServiceTest {

    @Inject
    JwtTokenService jwtTokenService;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    JWTParser jwtParser;

    @Test
    void should_generate_valid_token_for_user() throws ParseException {
        User user = userRepository.findByEmail(SUPER_ADMIN_EMAIL).orElseThrow();

        String token = jwtTokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts (header.payload.signature");

        JsonWebToken jwt = jwtParser.parse(token);

        assertEquals(user.email, jwt.getClaim("upn"));
        assertEquals(user.id.toString(), jwt.getSubject());
        assertEquals(user.fullName, jwt.getClaim("name"));
        assertEquals(user.email, jwt.getClaim("email"));
        assertTrue(jwt.getClaim("enabled") == JsonValue.TRUE);

        Set<String> groups = jwt.getGroups();
        assertNotNull(groups);
        assertTrue(groups.contains("SUPER_ADMIN"));

        var privileges = jwt.getClaim("privileges");
        assertNotNull(privileges);

    }

    @Test
    void should_include_all_user_roles() throws ParseException {
        User user = userRepository.findByEmailWithRoles(SUPER_ADMIN_EMAIL).orElseThrow();

        String token = jwtTokenService.generateToken(user);

        JsonWebToken jwt = jwtParser.parse(token);
        Set<String> groups = jwt.getGroups();

        assertEquals(1, groups.size());
        assertTrue(groups.contains("SUPER_ADMIN"));
    }

    @Test
    @Transactional
    void should_include_all_privileges_from_all_roles() throws ParseException {
        User user = userRepository.findByEmailWithRolesAndPrivileges(SUPER_ADMIN_EMAIL).orElseThrow();
        String token = jwtTokenService.generateToken(user);

        JsonWebToken jwt = jwtParser.parse(token);
        List<String> privileges = ((List<?>) jwt.getClaim("privileges"))
                .stream()
                .map(p -> ((JsonString) p).getString())
                .toList();

        assertNotNull(privileges);
        assertFalse(privileges.isEmpty());

        assertTrue(privileges.size() > 30, "SUPER_ADMIN should have all privileges");

        assertTrue(privileges.contains("CREATE_PRODUCT"), "SUPER_ADMIN should have CREATE_PRODUCT privileges");
        assertTrue(privileges.contains("DELETE_USER"), "SUPER_ADMIN should have DELETE_USER privileges");
        assertTrue(privileges.contains("VIEW_REPORTS"), "SUPER_ADMIN should have VIEW_REPORTS privileges");
    }

    @Test
    void should_generate_token_with_custom_expiration() throws ParseException {
        User user = userRepository.findByEmail(SUPER_ADMIN_EMAIL).orElseThrow();
        long customExpiration = 3600;

        String token = jwtTokenService.generateToken(user, customExpiration);

        JsonWebToken jwt = jwtParser.parse(token);
        assertNotNull(jwt.getExpirationTime());

        long expectedExpTime = System.currentTimeMillis() / 1000 + customExpiration;
        long actualExpTime = jwt.getExpirationTime();

        assertTrue(Math.abs(expectedExpTime - actualExpTime) < 5);
    }

    @Test
    void should_set_correct_issuer() throws ParseException {
        User user = userRepository.findByEmail(SUPER_ADMIN_EMAIL).orElseThrow();

        String token = jwtTokenService.generateToken(user);

        JsonWebToken jwt = jwtParser.parse(token);
        assertEquals("http://localhost:8080", jwt.getIssuer());
    }
}
