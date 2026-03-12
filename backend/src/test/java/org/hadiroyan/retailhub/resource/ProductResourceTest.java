package org.hadiroyan.retailhub.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.dto.response.ProductResponse;
import org.hadiroyan.retailhub.service.ProductService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

@QuarkusTest
public class ProductResourceTest {

    @InjectMock
    ProductService productService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID productId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    // Create product
    @Test
    @TestSecurity(user = "11111111-1111-1111-1111-111111111111", roles = "OWNER")
    void should_create_product_success() {
        ProductDetailResponse response = new ProductDetailResponse();
        response.sku = "SKU-001";
        response.name = "iPhone 15";
        response.costPrice = BigDecimal.valueOf(12000);

        when(currentUser.getUserId()).thenReturn(userId);
        when(productService.createProduct(eq(storeId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "sku": "SKU-001",
                            "name": "iPhone 15",
                            "price": 15000
                        }
                        """)
                .when()
                .post("/api/v1/stores/" + storeId + "/products")
                .then()
                .statusCode(201)
                .body("data.sku", equalTo("SKU-001"))
                .body("data.name", equalTo("iPhone 15"))
                .body("data.costPrice", notNullValue());
    }

    @Test
    void should_failed_create_product_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "sku": "SKU-001",
                            "name": "iPhone 15",
                            "price": 15000
                        }
                        """)
                .when()
                .post("/api/v1/stores/" + storeId + "/products")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "staff@example.com", roles = "STAFF")
    void should_failed_create_product_forbidden_for_staff() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "sku": "SKU-001",
                            "name": "iPhone 15",
                            "price": 15000
                        }
                        """)
                .when()
                .post("/api/v1/stores/" + storeId + "/products")
                .then()
                .statusCode(403);
    }

    // Get list products (public)
    @Test
    void should_return_product_list_success() {
        ProductResponse product = new ProductResponse();
        product.sku = "SKU-001";
        product.name = "iPhone 15";

        when(productService.listProducts(storeId, null, null, null, 0, 10))
                .thenReturn(new PagedResponse<>(List.of(product), 0, 10, 1));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/products")
                .then()
                .statusCode(200)
                .body("data.content[0].name", equalTo("iPhone 15"))
                .body("data.content[0].costPrice", nullValue());
    }

    @Test
    void should_filter_products_by_name() {
        ProductResponse product = new ProductResponse();
        product.name = "iPhone 15";

        when(productService.listProducts(storeId, "iphone", null, null, 0, 10))
                .thenReturn(new PagedResponse<>(List.of(product), 0, 10, 1));

        given()
                .queryParam("name", "iphone")
                .when()
                .get("/api/v1/stores/" + storeId + "/products")
                .then()
                .statusCode(200)
                .body("data.content", hasSize(1));
    }

    // Get product by SKU (public)
    @Test
    void should_get_product_by_sku_success() {
        ProductResponse response = new ProductResponse();
        response.sku = "SKU-001";
        response.name = "iPhone 15";

        when(productService.getProductBySku(storeId, "SKU-001")).thenReturn(response);

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/products/SKU-001")
                .then()
                .statusCode(200)
                .body("data.sku", equalTo("SKU-001"))
                .body("data.costPrice", nullValue());
    }

    // Get product (Internal)

    @Test
    @TestSecurity(user = "11111111-1111-1111-1111-111111111111", roles = "OWNER")
    void should_get_product_detail_success_for_owner() {
        ProductDetailResponse response = new ProductDetailResponse();
        response.sku = "SKU-001";
        response.name = "iPhone 15";
        response.costPrice = BigDecimal.valueOf(12000);

        when(currentUser.getUserId()).thenReturn(userId);
        when(productService.getProductDetail(storeId, "SKU-001", userId)).thenReturn(response);

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/products/SKU-001/detail")
                .then()
                .statusCode(200)
                .body("data.costPrice", notNullValue()); // with costPrice in response
    }

    @Test
    void should_failed_get_product_detail_unauthorized() {
        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/products/SKU-001/detail")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "customer@example.com", roles = "CUSTOMER")
    void should_failed_get_product_detail_forbidden_for_customer() {
        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/products/SKU-001/detail")
                .then()
                .statusCode(403);
    }

    // Update product
    @Test
    @TestSecurity(user = "11111111-1111-1111-1111-111111111111", roles = "OWNER")
    void should_update_product_success() {
        ProductDetailResponse response = new ProductDetailResponse();
        response.name = "iPhone 15 Pro";

        when(currentUser.getUserId()).thenReturn(userId);
        when(productService.updateProduct(eq(storeId), eq(productId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "iPhone 15 Pro",
                            "price": 18000
                        }
                        """)
                .when()
                .put("/api/v1/stores/" + storeId + "/products/" + productId)
                .then()
                .statusCode(200)
                .body("data.name", equalTo("iPhone 15 Pro"));
    }

    @Test
    void should_failed_update_product_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "iPhone 15 Pro",
                            "price": 18000
                        }
                        """)
                .when()
                .put("/api/v1/stores/" + storeId + "/products/" + productId)
                .then()
                .statusCode(401);
    }

    // Delete product
    @Test
    @TestSecurity(user = "11111111-1111-1111-1111-111111111111", roles = "OWNER")
    void should_delete_product_success() {
        when(currentUser.getUserId()).thenReturn(userId);
        doNothing().when(productService).deleteProduct(storeId, productId, userId);

        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/products/" + productId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Product deleted successfully"));
    }

    @Test
    void should_failed_delete_product_unauthorized() {
        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/products/" + productId)
                .then()
                .statusCode(401);
    }
}