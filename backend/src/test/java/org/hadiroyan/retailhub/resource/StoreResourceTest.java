package org.hadiroyan.retailhub.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.StoreResponse;
import org.hadiroyan.retailhub.service.StoreService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class StoreResourceTest {

    @InjectMock
    StoreService storeService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    // Create store
    @Test
    @TestSecurity(user = "owner@example.com", roles = "OWNER")
    void should_create_store_success() {

        StoreResponse response = new StoreResponse();
        response.name = "Toko Maju";

        when(currentUser.getEmail()).thenReturn("owner@example.com");
        when(storeService.createStore(anyString(), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Toko Maju\" }")
                .when()
                .post("/api/v1/stores")
                .then()
                .statusCode(201)
                .body("data.name", equalTo("Toko Maju"));
    }

    @Test
    void should_failed_create_store_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Toko Maju\" }")
                .when()
                .post("/api/v1/stores")
                .then()
                .statusCode(401);
    }

    // List store
    @Test
    void should_return_list_store_public_success() {

        when(currentUser.getUserIdOptional()).thenReturn(Optional.empty());
        when(currentUser.getRoles()).thenReturn(Set.of());

        when(storeService.listsStores(null, Set.of(), 0, 10))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        given()
                .when()
                .get("/api/v1/stores")
                .then()
                .statusCode(200);
    }

    // Update store
    @Test
    @TestSecurity(user = "11111111-1111-1111-1111-111111111111", roles = "OWNER")
    void should_update_store_success() {

        when(currentUser.getUserId()).thenReturn(userId);
        when(currentUser.getRoles()).thenReturn(Set.of("OWNER"));

        when(storeService.updateStore(any(), any(), any(), any()))
                .thenReturn(new StoreResponse());

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Updated\" }")
                .when()
                .put("/api/v1/stores/" + storeId)
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "staff@example.com", roles = "STAFF")
    void should_forbidden_when_staff_update_store() {

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Updated\" }")
                .when()
                .put("/api/v1/stores/" + storeId)
                .then()
                .statusCode(403);
    }
}