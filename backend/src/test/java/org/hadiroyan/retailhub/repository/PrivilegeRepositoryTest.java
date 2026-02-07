package org.hadiroyan.retailhub.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.hadiroyan.retailhub.model.Privilege;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class PrivilegeRepositoryTest {

    @Inject
    PrivilegeRepository privilegeRepository;

    @Test
    void should_find_privilege_by_name_and_exists() {
        String privilege = "CREATE_USER";

        Optional<Privilege> optPrivilege = privilegeRepository.findByName(privilege);
        assertTrue(optPrivilege.isPresent());

        assertTrue(privilegeRepository.existsByName(privilege));
    }

    @Test
    void should_find_privileges_by_resource() {
        List<Privilege> privileges = privilegeRepository.findByResource("USER");

        assertFalse(privileges.isEmpty());
        privileges.forEach(p -> assertEquals("USER", p.resource));
    }

    @Test
    void should_find_privileges_by_names() {
        List<String> names = List.of(
                "CREATE_USER",
                "READ_USER");

        List<Privilege> privileges = privilegeRepository.findByNames(names);
        assertEquals(names.size(), privileges.size());
    }

}
