package org.hadiroyan.retailhub.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class PasswordServiceTest {

    @Inject
    PasswordService passwordService;

    @Test
    void testHashAndVerify() {
        String rawPassword = "secretPassword123";
        String hashed = passwordService.hash(rawPassword);
        
        assertNotNull(hashed);
        assertTrue(passwordService.verify(rawPassword, hashed));
        assertFalse(passwordService.verify("wrongPassword", hashed));
    }
}
