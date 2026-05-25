package org.hadiroyan.retailhub.resource;

import static org.hadiroyan.retailhub.util.TestConstanst.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.LoginRequest;
import org.hadiroyan.retailhub.dto.request.RegisterCustomerRequest;
import org.hadiroyan.retailhub.dto.request.RegisterOwnerRequest;
import org.hadiroyan.retailhub.dto.request.VerifyOtpRequest;
import org.hadiroyan.retailhub.model.User;
import org.hadiroyan.retailhub.repository.EmailVerificationTokenRepository;
import org.hadiroyan.retailhub.repository.UserRepository;
import org.hadiroyan.retailhub.service.AuthService;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
class AuthResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    AuthService authService;

    @Inject
    EmailVerificationTokenRepository tokenRepository;

    private static final String BASE_PATH = "/api/auth";

    @Test
    void should_login_successfully_with_valid_credentials() {
        LoginRequest request = new LoginRequest();
        request.email = SUPER_ADMIN_EMAIL;
        request.password = TEST_PASSWORD;

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("message", equalTo("Login successful"))
                .body("data.token", notNullValue())
                .body("data.user.email", equalTo(SUPER_ADMIN_EMAIL))
                .extract()
                .response();

        String jwtCookie = response.getCookie("jwt");
        assertNotNull(jwtCookie, "JWT cookie should be set");
        assertFalse(jwtCookie.isEmpty());
    }

    @Test
    void should_fail_login_with_wrong_password() {
        LoginRequest request = new LoginRequest();
        request.email = SUPER_ADMIN_EMAIL;
        request.password = "WrongPassword";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("message", containsString("Invalid email or password"));
    }

    @Test
    void should_fail_login_with_nonexistent_email() {
        LoginRequest request = new LoginRequest();
        request.email = "nonexistent@test.com";
        request.password = TEST_PASSWORD;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(401)
                .body("status", equalTo(401));
    }

    @Test
    void should_fail_login_for_disabled_account() {
        LoginRequest request = new LoginRequest();
        request.email = DISABLED_USER_EMAIL;
        request.password = TEST_PASSWORD;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(401)
                .body("status", equalTo(401));
    }

    @Test
    @Transactional
    void should_register_customer_successfully_and_auto_login() {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.email = "newcustomer@integration.test";
        request.password = "SecurePass123";
        request.fullName = "New Customer";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/register-customer")
                .then()
                .statusCode(201)
                .extract()
                .response();

        String jwtCookie = response.getCookie("jwt");
        assertNotNull(jwtCookie, "JWT cookie should be set after registration (auto-login)");

        userRepository.findByEmail("newcustomer@integration.test")
                .ifPresent(user -> userRepository.delete(user));
    }

    @Test
    void should_fail_register_customer_with_existing_email() {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.email = SUPER_ADMIN_EMAIL; // Already exists
        request.password = "password123";
        request.fullName = "Test User";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/register-customer")
                .then()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("message", containsString(SUPER_ADMIN_EMAIL));
    }

    @Test
    void should_fail_register_customer_with_invalid_email() {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.email = "invalid-email"; // No @ symbol
        request.password = "password123";
        request.fullName = "Test User";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/register-customer")
                .then()
                .statusCode(400)
                .body("status", equalTo(400));
    }

    @Test
    @Transactional
    void should_register_owner_successfully_and_auto_login() {
        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = "newowner@integration.test";
        request.password = "SecurePass123";
        request.fullName = "New Owner";

        Response response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/register-owner")
                .then()
                .statusCode(201)
                .body("status", equalTo(201))
                .body("data.token", notNullValue())
                .body("data.user.email", equalTo("newowner@integration.test"))
                .extract()
                .response();

        String jwtCookie = response.getCookie("jwt");
        assertNotNull(jwtCookie);

        userRepository.findByEmail("newowner@integration.test")
                .ifPresent(user -> userRepository.delete(user));
    }

    @Test
    void should_fail_register_owner_with_weak_password() {
        RegisterOwnerRequest request = new RegisterOwnerRequest();
        request.email = "test@example.com";
        request.password = "weak"; // Too short
        request.fullName = "Test User";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/register-owner")
                .then()
                .statusCode(400)
                .body("status", equalTo(400));
    }

    @Test
    void should_get_current_user_with_valid_jwt() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = SUPER_ADMIN_EMAIL;
        loginRequest.password = TEST_PASSWORD;

        String jwtCookie = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("jwt");

        given()
                .cookie("jwt", jwtCookie)
                .when()
                .get(BASE_PATH + "/me")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("message", containsString("Current user retrieved successfully"))
                .body("data.email", equalTo(SUPER_ADMIN_EMAIL))
                .body("data.enabled", equalTo(true));
    }

    @Test
    void should_fail_get_current_user_without_jwt() {
        given()
                .when()
                .get(BASE_PATH + "/me")
                .then()
                .statusCode(401); // Unauthorized
    }

    @Test
    void should_fail_get_current_user_with_invalid_jwt() {
        String invalidJwt = "invalid.jwt.token";

        given()
                .cookie("jwt", invalidJwt)
                .when()
                .get(BASE_PATH + "/me")
                .then()
                .statusCode(401); // Unauthorized
    }

    @Test
    void should_logout_successfully() {

        Response response = given()
                .when()
                .post(BASE_PATH + "/logout")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("message", equalTo("Logout successful"))
                .extract()
                .response();

        assertNotNull(response.getDetailedCookie("jwt"), "JWT cookie should be present");
    }

    @Test
    @Transactional
    void should_complete_full_auth_flow() {
        String testEmail = "flowtest@integration.test";

        // Step 1: Register customer
        RegisterCustomerRequest registerRequest = new RegisterCustomerRequest();
        registerRequest.email = testEmail;
        registerRequest.password = "FlowTest123";
        registerRequest.fullName = "Flow Test User";

        String registerCookie = given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                .post(BASE_PATH + "/register-customer")
                .then()
                .statusCode(201)
                .extract()
                .cookie("jwt");

        assertNotNull(registerCookie, "Should get JWT after registration");

        // Step 2: Access /me with registration JWT
        given()
                .cookie("jwt", registerCookie)
                .when()
                .get(BASE_PATH + "/me")
                .then()
                .statusCode(200)
                .body("data.email", equalTo(testEmail));

        // Step 3: Logout
        given()
                .when()
                .post(BASE_PATH + "/logout")
                .then()
                .statusCode(200);

        // Step 4: Login again
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.email = testEmail;
        loginRequest.password = "FlowTest123";

        String loginCookie = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("jwt");

        assertNotNull(loginCookie, "Should get JWT after login");

        // Step 5: Access /me again
        given()
                .cookie("jwt", loginCookie)
                .when()
                .get(BASE_PATH + "/me")
                .then()
                .statusCode(200)
                .body("data.email", equalTo(testEmail));

        userRepository.findByEmail(testEmail)
                .ifPresent(user -> userRepository.delete(user));
    }

    // verify-email
    @Test
    void should_verify_email_successfully_with_valid_otp() {
        // Step 1: login dulu untuk dapat JWT
        String jwtCookie = loginAndGetCookie(CUSTOMER_EMAIL, TEST_PASSWORD);

        // Step 2: generate OTP
        User user = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        authService.generateAndSendOtp(user);

        // Step 3: ambil OTP dari DB
        String otp = tokenRepository
                .findLatestActiveByUserId(user.id)
                .orElseThrow().otp;

        // Step 4: hit endpoint
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.otp = otp;

        given()
                .cookie("jwt", jwtCookie)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/verify-email")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("message", equalTo("Email verified successfully"));

        // cleanup
        resetVerifiedState(user.id, CUSTOMER_EMAIL);
    }

    @Test
    void should_fail_verify_email_with_wrong_otp() {
        String jwtCookie = loginAndGetCookie(CUSTOMER_EMAIL, TEST_PASSWORD);

        User user = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        authService.generateAndSendOtp(user);

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.otp = "000000"; // salah

        given()
                .cookie("jwt", jwtCookie)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/verify-email")
                .then()
                .statusCode(400)
                .body("status", equalTo(400));

        // cleanup
        resetVerifiedState(user.id, CUSTOMER_EMAIL);
    }

    @Test
    void should_fail_verify_email_without_jwt() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.otp = "123456";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/verify-email")
                .then()
                .statusCode(401);
    }

    // resend-otp
    @Test
    void should_resend_otp_successfully() {
        String jwtCookie = loginAndGetCookie(CUSTOMER_EMAIL, TEST_PASSWORD);

        given()
                .cookie("jwt", jwtCookie)
                .when()
                .post(BASE_PATH + "/resend-otp")
                .then()
                .statusCode(200)
                .body("status", equalTo(200))
                .body("message", equalTo("OTP sent successfully"));

        // cleanup
        User user = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        resetVerifiedState(user.id, CUSTOMER_EMAIL);
    }

    @Test
    void should_fail_resend_otp_without_jwt() {
        given()
                .when()
                .post(BASE_PATH + "/resend-otp")
                .then()
                .statusCode(401);
    }

    @Test
    void should_fail_resend_otp_when_already_verified() {
        // OWNER sudah email_verified = true di seed data
        String jwtCookie = loginAndGetCookie(OWNER_EMAIL, TEST_PASSWORD);

        given()
                .cookie("jwt", jwtCookie)
                .when()
                .post(BASE_PATH + "/resend-otp")
                .then()
                .statusCode(400)
                .body("status", equalTo(400));
    }

    private String loginAndGetCookie(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.email = email;
        request.password = password;

        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(BASE_PATH + "/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("jwt");
    }

    @Transactional
    public void resetVerifiedState(UUID userId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.emailVerified = false;
        tokenRepository.delete("user.id", userId);
    }
}
