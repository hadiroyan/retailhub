package org.hadiroyan.retailhub.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.hadiroyan.retailhub.model.Privilege;
import org.hadiroyan.retailhub.model.Role;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class RoleRepositoryTest {

    @Inject
    RoleRepository roleRepository;

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN",
            "ADMIN",
            "OWNER",
            "MANAGER",
            "STAFF",
            "CUSTOMER",
    })
    void should_exist_for_seeded_roles(String roleName) {
        boolean isExists = roleRepository.existsByName(roleName);
        assertTrue(isExists, roleName + " role should be available from the seed data.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "USER",
            "GUEST",
            "ANONYMOUS",
    })
    void should_not_exists_for_seeded_roles(String roleName) {
        boolean isNotExists = roleRepository.existsByName(roleName);
        assertFalse(isNotExists, roleName + " role should not be available from the initial data.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN",
            "ADMIN",
            "OWNER",
            "MANAGER",
            "STAFF",
            "CUSTOMER",
    })
    void should_load_role_with_privileges(String roleName) {
        Optional<Role> optRole = roleRepository.findByNameWithPrivileges(roleName);
        assertTrue(optRole.isPresent(), roleName + " role should exists.");

        Role role = optRole.get();
        assertFalse(role.privileges.isEmpty(), roleName + " should have at least one privilege.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN",
            "ADMIN",
            "OWNER",
            "MANAGER",
            "STAFF",
            "CUSTOMER",
    })
    void should_return_true_when_role_has_privilege(String roleName) {
        Optional<Role> optRole = roleRepository.findByNameWithPrivileges(roleName);
        assertTrue(optRole.isPresent(), roleName + " role should exists.");

        Role role = optRole.get();
        assertFalse(role.privileges.isEmpty(),
                roleName + " should have at least one privilege.");

        Privilege privilege = role.privileges.iterator().next();
        boolean hasPrivilege = roleRepository.roleHasPrivilege(
                role.id,
                privilege.name);

        assertTrue(
                hasPrivilege,
                roleName + " should have privilege " + privilege.name);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN",
            "ADMIN",
            "OWNER",
            "MANAGER",
            "STAFF",
            "CUSTOMER",
    })
    void should_return_false_when_role_does_not_have_privilege(String roleName) {
        Optional<Role> optRole = roleRepository.findByName(roleName);
        assertTrue(optRole.isPresent(), roleName + " role should exist");

        Role role = optRole.get();

        String unknownPrivilege = "ALL_ACCESS";
        boolean hasPrivilege = roleRepository.roleHasPrivilege(
                role.id,
                unknownPrivilege);

        assertFalse(
                hasPrivilege,
                roleName + " should NOT have privilege " + unknownPrivilege);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SUPER_ADMIN",
            "ADMIN",
            "OWNER",
            "MANAGER",
            "STAFF",
            "CUSTOMER",
    })
    void should_not_return_duplicate_roles_when_fetching_privileges(String roleName) {
        Optional<Role> optRole = roleRepository.findByNameWithPrivileges(roleName);
        assertTrue(optRole.isPresent(), roleName + " role should exist");

        Role role = optRole.get();
        assertFalse(role.privileges.isEmpty(),
                roleName + " should have at least one privilege");

        assertTrue(role.privileges.size() >= 1);
    }
}
