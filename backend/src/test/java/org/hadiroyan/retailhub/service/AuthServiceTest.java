package org.hadiroyan.retailhub.service;

import static org.hadiroyan.retailhub.util.TestConstanst.CUSTOMER_EMAIL;
import static org.hadiroyan.retailhub.util.TestConstanst.CUSTOMER_ID;
import static org.hadiroyan.retailhub.util.TestConstanst.DISABLED_USER_EMAIL;
import static org.hadiroyan.retailhub.util.TestConstanst.SUPER_ADMIN_EMAIL;
import static org.hadiroyan.retailhub.util.TestConstanst.TEST_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.LoginRequest;
import org.hadiroyan.retailhub.dto.request.RegisterCustomerRequest;
import org.hadiroyan.retailhub.dto.request.RegisterOwnerRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.AuthResponse;
import org.hadiroyan.retailhub.dto.response.UserResponse;
import org.hadiroyan.retailhub.exception.AccountDisabledException;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.hadiroyan.retailhub.exception.EmailAlreadyExistsException;
import org.hadiroyan.retailhub.exception.InvalidEmailFormatException;
import org.hadiroyan.retailhub.exception.NotFoundException;
import org.hadiroyan.retailhub.exception.UnauthorizedException;
import org.hadiroyan.retailhub.exception.ValidationException;
import org.hadiroyan.retailhub.model.EmailVerificationToken;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.repository.EmailVerificationTokenRepository;
import org.hadiroyan.retailhub.repository.RoleRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.hadiroyan.retailhub.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    AuthService authService;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    UserRoleRepository userRoleRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Inject
    EntityManager entityManager;

    @Test
    void should_success_login_with_valid_credentials() {

        LoginRequest request = new LoginRequest();
        request.email = SUPER_ADMIN_EMAIL;
        request.password = TEST_PASSWORD;

        ApiResponse<AuthResponse> response = authService.login(request);

        assertNotNull(response);
        assertEquals(200, response.status);
        assertEquals("Login successful", response.message);
        assertNotNull(response.data);
        assertNotNull(response.data.token, "JWT Token should be generated");
        assertNotNull(response.data.user);
        assertEquals(SUPER_ADMIN_EMAIL, response.data.user.email);
        assertTrue(response.data.user.enabled);
    }

    @Test
    void should_failed_login_with_invalid_emails() {
        LoginRequest request = new LoginRequest();
        request.email = "someuseremail@gmail.com";
        request.password = TEST_PASSWORD;

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void should_failed_login_with_invalid_password() {
        LoginRequest request = new LoginRequest();
        request.email = SUPER_ADMIN_EMAIL;
        request.password = "ADMIN123";

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void should_failed_login_for_disabled_account() {
        LoginRequest request = new LoginRequest();
        request.email = DISABLED_USER_EMAIL;
        request.password = TEST_PASSWORD;

        assertThrows(AccountDisabledException.class, () -> authService.login(request));
    }

    @Test
    void should_success_register_owner_with_valid_data() {
        String NEW_OWNER_EMAIL = "newowner@test.com";
        String NEW_OWNER_PASS = "newownerpass";
        String NEW_OWNER_NAME = "New Owner";

        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = NEW_OWNER_EMAIL;
        request.password = NEW_OWNER_PASS;
        request.fullName = NEW_OWNER_NAME;

        ApiResponse<UserResponse> response = authService.registerOwner(request);

        assertNotNull(response);
        assertEquals(201, response.status);
        assertEquals("Account created successfully", response.message);
        assertNotNull(response.data);
        assertEquals(NEW_OWNER_EMAIL, response.data.email);
        assertEquals(NEW_OWNER_NAME, response.data.fullName);
        assertTrue(response.data.enabled);

        Optional<User> savedUser = userRepository.findByEmail(NEW_OWNER_EMAIL);
        assertTrue(savedUser.isPresent(), "User should be available in database");
        assertEquals(NEW_OWNER_EMAIL, savedUser.get().email);

        assertNotEquals(NEW_OWNER_PASS, savedUser.get().password);
        assertTrue(passwordService.verify(NEW_OWNER_PASS, savedUser.get().password));

        assertTrue(savedUser.get().userRoles.stream().anyMatch(ur -> ur.role.name.equals("OWNER")));

        cleanupUser(NEW_OWNER_EMAIL);
    }

    @Test
    void should_failed_register_owner_with_existing_email() {
        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = SUPER_ADMIN_EMAIL;
        request.password = TEST_PASSWORD;
        request.fullName = "Test User";

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.registerOwner(request));

        assertTrue(exception.getMessage().contains(SUPER_ADMIN_EMAIL));
    }

    @Test
    void should_failed_register_owner_with_invalid_email_format() {
        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = "invalid-email";
        request.password = "password123";
        request.fullName = "Test User";

        assertThrows(
                InvalidEmailFormatException.class,
                () -> authService.registerOwner(request));
    }

    @Test
    void should_failed_register_owner_with_empty_name() {
        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = "validemail@test.com";
        request.password = "password123";
        request.fullName = "";

        assertThrows(
                ValidationException.class,
                () -> authService.registerOwner(request));
    }

    @Test
    void should_success_register_customer_with_valid_data() {
        String NEW_CUSTOMER_EMAIL = "newcustomer@test.com";
        String NEW_CUSTOMER_PASS = "newcustomerpass";
        String NEW_CUSTOMER_NAME = "New Customer";

        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.email = NEW_CUSTOMER_EMAIL;
        request.password = NEW_CUSTOMER_PASS;
        request.fullName = NEW_CUSTOMER_NAME;

        ApiResponse<UserResponse> response = authService.registerCustomer(request);

        assertNotNull(response);
        assertEquals(201, response.status);
        assertEquals("Account created successfully", response.message);
        assertNotNull(response.data);
        assertEquals(NEW_CUSTOMER_EMAIL, response.data.email);
        assertEquals(NEW_CUSTOMER_NAME, response.data.fullName);
        assertTrue(response.data.enabled);

        Optional<User> savedUser = userRepository.findByEmail(NEW_CUSTOMER_EMAIL);
        assertTrue(savedUser.isPresent(), "User should be available in database");
        assertEquals(NEW_CUSTOMER_EMAIL, savedUser.get().email);

        assertNotEquals(NEW_CUSTOMER_EMAIL, savedUser.get().password);
        assertTrue(passwordService.verify(NEW_CUSTOMER_PASS, savedUser.get().password));

        assertTrue(savedUser.get().userRoles.stream().anyMatch(ur -> ur.role.name.equals("CUSTOMER")));

        cleanupUser(NEW_CUSTOMER_EMAIL);
    }

    @Test
    void should_trim_from_fullname() {

        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = "trimtest@test.com";
        request.password = "password123";
        request.fullName = "  My User Test  "; // Whitespace

        ApiResponse<UserResponse> response = authService.registerOwner(request);

        assertEquals("My User Test", response.data.fullName); // Trimmed

        cleanupUser("trimtest@test.com");
    }

    @Test
    void should_get_current_user_successfully() {
        UserResponse response = authService.getCurrentUser(SUPER_ADMIN_EMAIL);

        assertNotNull(response);
        assertEquals(SUPER_ADMIN_EMAIL, response.email);
        assertTrue(response.enabled);
        assertNotNull(response.roles);
        assertTrue(response.roles.contains("SUPER_ADMIN"));
    }

    @Test
    void should_fail_get_current_user_for_nonexistent_email() {
        String email = "nonexistent@test.com";

        assertThrows(NotFoundException.class, () -> authService.getCurrentUser(email));
    }

    @Test
    void should_generate_token_for_user() {
        User user = userRepository.findByEmailWithRolesAndPrivileges(SUPER_ADMIN_EMAIL)
                .orElseThrow();

        String token = authService.generateTokenForUser(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts");
    }

    // OTP — verifyOtp
    @Test
    void should_success_verify_otp_with_valid_otp() {
        String email = CUSTOMER_EMAIL;
        User user = userRepository.findByEmail(email).orElseThrow();

        // generate OTP manually because the registration flow does not yet include OTP
        authService.generateAndSendOtp(user);

        EmailVerificationToken token = emailVerificationTokenRepository
                .findLatestActiveByUserId(user.id)
                .orElseThrow();

        authService.verifyOtp(email, token.otp);

        entityManager.clear();

        assertTrue(userRepository.findByEmail(email).orElseThrow().emailVerified);

        // reset user state and token
        resetVerifiedState(user.id, user.email);
    }

    @Test
    void should_fail_verify_otp_with_wrong_otp() {
        String email = CUSTOMER_EMAIL;
        User user = userRepository.findByEmail(email).orElseThrow();

        authService.generateAndSendOtp(user);

        assertThrows(BadRequestException.class,
                () -> authService.verifyOtp(email, "000000"));

        resetVerifiedState(user.id, user.email);
    }

    @Test
    void should_fail_verify_otp_when_already_verified() {
        String email = CUSTOMER_EMAIL;

        setEmailVerified(CUSTOMER_EMAIL, true);

        authService.generateAndSendOtp(
                userRepository.findByEmail(email).orElseThrow());

        EmailVerificationToken token = emailVerificationTokenRepository
                .findLatestActiveByUserId(CUSTOMER_ID)
                .orElseThrow();

        assertThrows(BadRequestException.class,
                () -> authService.verifyOtp(email, token.otp));

        resetVerifiedState(CUSTOMER_ID, email);
    }

    @Test
    void should_fail_verify_otp_when_no_active_token() {
        String email = CUSTOMER_EMAIL;
        // does not generate an OTP at all

        assertThrows(BadRequestException.class,
                () -> authService.verifyOtp(email, "123456"));
    }

    @Test
    void should_success_resend_otp() {
        String email = CUSTOMER_EMAIL;
        User user = userRepository.findByEmail(email).orElseThrow();

        authService.generateAndSendOtp(user);

        String firstOtp = emailVerificationTokenRepository
                .findLatestActiveByUserId(user.id)
                .orElseThrow().otp;

        authService.resendOtp(email);

        String newOtp = emailVerificationTokenRepository
                .findLatestActiveByUserId(user.id)
                .orElseThrow().otp;

        assertNotEquals(firstOtp, newOtp);

        resetVerifiedState(user.id, user.email);
    }

    @Test
    void should_fail_resend_otp_when_already_verified() {
        String email = CUSTOMER_EMAIL;
        setEmailVerified(email, true);

        assertThrows(BadRequestException.class,
                () -> authService.resendOtp(email));

        resetVerifiedState(CUSTOMER_ID, email);
    }

    @Test
    void should_fail_resend_otp_when_rate_limit_exceeded() {
        String email = CUSTOMER_EMAIL;
        User user = userRepository.findByEmail(email).orElseThrow();

        // Generate 5 OTPs — use `generateAndSendOtp` directly, not `resendOtp`
        // to avoid hitting the rate limit midway
        for (int i = 0; i < 5; i++) {
            authService.generateAndSendOtp(user);
        }

        // The 6th request via resendOTP must hit the rate limit
        assertThrows(BadRequestException.class,
                () -> authService.resendOtp(email));

        resetVerifiedState(user.id, user.email);
    }

    @Transactional
    public void setEmailVerified(String email, boolean verified) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.emailVerified = verified;
    }

    @Transactional
    public void resetVerifiedState(UUID userId, String email) {
        // Set “emailVerified” to false
        User user = userRepository.findByEmail(email).orElseThrow();
        user.emailVerified = false;
        // Delete all tokens
        emailVerificationTokenRepository.delete("user.id", userId);
    }

    @Transactional
    public void cleanupUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            emailVerificationTokenRepository.delete("user.id", user.id);
            userRepository.delete(user);
        });
    }
}
