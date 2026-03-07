package org.hadiroyan.retailhub.resource;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.CategoryResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.CategoryService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CategoryResourceTest {

    @InjectMock
    CategoryService categoryService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID categoryId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // Create category
    @Test
    @TestSecurity(user = "owner@example.com", roles = "OWNER")
    void should_create_category_success() {
        CategoryResponse response = new CategoryResponse();
        response.name = "Electronics";
        response.slug = "electronics";

        when(currentUser.getUserId()).thenReturn(userId);
        when(categoryService.createCategory(eq(storeId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Electronics\" }")
                .when()
                .post("/api/v1/stores/" + storeId + "/categories")
                .then()
                .statusCode(201)
                .body("data.name", equalTo("Electronics"));
    }

    @Test
    void should_failed_create_category_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Electronics\" }")
                .when()
                .post("/api/v1/stores/" + storeId + "/categories")
                .then()
                .statusCode(401);
    }

    // Get list categories
    @Test
    void should_return_category_list_success() {

        CategoryResponse category = new CategoryResponse();
        category.name = "Electronics";

        when(categoryService.listCategories(storeId, 0, 10))
                .thenReturn(new PagedResponse<>(List.of(category), 0, 10, 1));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/categories")
                .then()
                .statusCode(200)
                .body("data.content[0].name", equalTo("Electronics"));
    }

    // Get category by slug
    @Test
    void should_get_category_by_slug_success() {

        CategoryResponse response = new CategoryResponse();
        response.name = "Electronics";
        response.slug = "electronics";

        when(categoryService.getCategoryBySlug(storeId, "electronics"))
                .thenReturn(response);

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/categories/electronics")
                .then()
                .statusCode(200)
                .body("data.slug", equalTo("electronics"));
    }

    // Update category
    @Test
    @TestSecurity(user = "owner@example.com", roles = "OWNER")
    void should_update_category_success() {

        CategoryResponse response = new CategoryResponse();
        response.name = "Updated";

        when(currentUser.getUserId()).thenReturn(userId);
        when(categoryService.updateCategory(eq(storeId), eq(categoryId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("{ \"name\": \"Updated\" }")
                .when()
                .put("/api/v1/stores/" + storeId + "/categories/" + categoryId)
                .then()
                .statusCode(200);
    }

    // Delete category
    @Test
    @TestSecurity(user = "owner@example.com", roles = "OWNER")
    void should_delete_category_success() {

        when(currentUser.getUserId()).thenReturn(userId);
        doNothing().when(categoryService).deleteCategory(storeId, categoryId, userId);

        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/categories/" + categoryId)
                .then()
                .statusCode(200);
    }
}
