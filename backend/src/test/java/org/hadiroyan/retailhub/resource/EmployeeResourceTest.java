package org.hadiroyan.retailhub.resource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.EmployeeResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.EmployeeService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class EmployeeResourceTest {

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID targetUserId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // CREATE EMPLOYEE
    @Test
    @TestSecurity(user = "owner@example.com", roles = { "OWNER" })
    void should_create_employee_success() {

        EmployeeResponse response = new EmployeeResponse();
        response.id = targetUserId;

        when(currentUser.getUserId()).thenReturn(userId);
        when(currentUser.getRoles()).thenReturn(Set.of("OWNER"));

        when(employeeService.createEmployee(any(), any(), any(), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("{ \"email\": \"JohnDoe@test.com\", \"password\": \"JohnDoe123\", \"fullName\": \"John Doe\", \"role\": \"STAFF\" }")
                .when()
                .post("/api/v1/stores/" + storeId + "/employees")
                .then()
                .statusCode(201)
                .body("data.id", equalTo(targetUserId.toString()));
    }

    @Test
    void should_failed_create_employee_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"John Doe\", \"role\": \"STAFF\" }")
                .when()
                .post("/api/v1/stores/" + storeId + "/employees")
                .then()
                .statusCode(401);
    }

    // LIST EMPLOYEES
    @Test
    @TestSecurity(user = "manager@example.com", roles = { "MANAGER" })
    void should_list_employees_success() {

        when(currentUser.getUserId()).thenReturn(userId);

        when(employeeService.listEmployees(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/employees")
                .then()
                .statusCode(200);
    }

    // UPDATE EMPLOYEE
    @Test
    @TestSecurity(user = "owner@example.com", roles = { "OWNER" })
    void should_update_employee_success() {

        when(currentUser.getUserId()).thenReturn(userId);

        when(employeeService.updateEmployeeRole(any(), any(), any(), any()))
                .thenReturn(new EmployeeResponse());

        given()
                .contentType(ContentType.JSON)
                .body("{ \"role\": \"ADMIN\" }")
                .when()
                .put("/api/v1/stores/" + storeId + "/employees/" + targetUserId)
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "staff@example.com", roles = { "STAFF" })
    void should_forbidden_when_staff_update_employee() {

        given()
                .contentType(ContentType.JSON)
                .body("{ \"role\": \"ADMIN\" }")
                .when()
                .put("/api/v1/stores/" + storeId + "/employees/" + targetUserId)
                .then()
                .statusCode(403);
    }

    // DELETE EMPLOYEE
    @Test
    @TestSecurity(user = "admin@example.com", roles = { "ADMIN" })
    void should_delete_employee_success() {

        when(currentUser.getUserId()).thenReturn(userId);

        doNothing().when(employeeService)
                .deleteEmployee(any(), any(), any());

        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/employees/" + targetUserId)
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "staff@example.com", roles = { "STAFF" })
    void should_forbidden_when_staff_delete_employee() {

        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/employees/" + targetUserId)
                .then()
                .statusCode(403);
    }

    @Test
    void should_failed_delete_employee_unauthorized() {
        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/employees/" + targetUserId)
                .then()
                .statusCode(401);
    }
}
