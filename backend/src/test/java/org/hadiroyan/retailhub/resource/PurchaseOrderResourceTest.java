package org.hadiroyan.retailhub.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.PurchaseOrderResponse;
import org.hadiroyan.retailhub.service.PurchaseOrderService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

@QuarkusTest
public class PurchaseOrderResourceTest {

    @InjectMock
    PurchaseOrderService purchaseOrderService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID supplierId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    UUID orderId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    // CREATE
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_create_purchase_order_success() {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.id = orderId;
        response.orderNumber = "PO-20260501120000-ABCD";
        response.totalAmount = BigDecimal.valueOf(500000);

        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.createPurchaseOrder(eq(storeId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "supplierId": "%s",
                            "items": [
                                {
                                    "productId": "%s",
                                    "quantity": 10,
                                    "unitPrice": 50000
                                }
                            ]
                        }
                        """.formatted(supplierId, UUID.randomUUID()))
                .when()
                .post("/api/v1/stores/" + storeId + "/purchase-orders")
                .then()
                .statusCode(201)
                .body("data.orderNumber", equalTo("PO-20260501120000-ABCD"));
    }

    @Test
    void should_failed_create_purchase_order_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/v1/stores/" + storeId + "/purchase-orders")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "staff@test.com", roles = "STAFF")
    void should_failed_create_purchase_order_forbidden_for_staff() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/v1/stores/" + storeId + "/purchase-orders")
                .then()
                .statusCode(403);
    }

    // LIST
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_list_purchase_orders_success() {
        PurchaseOrderResponse order = new PurchaseOrderResponse();
        order.id = orderId;
        order.status = "PENDING";

        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.listPurchaseOrders(eq(storeId), eq(userId), isNull(), eq(0), eq(10)))
                .thenReturn(new PagedResponse<>(List.of(order), 0, 10, 1));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/purchase-orders")
                .then()
                .statusCode(200)
                .body("data.content", hasSize(1));
    }

    @Test
    @TestSecurity(user = "staff@test.com", roles = "STAFF")
    void should_list_purchase_orders_success_for_staff() {
        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.listPurchaseOrders(eq(storeId), eq(userId), isNull(), eq(0), eq(10)))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/purchase-orders")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_list_purchase_orders_with_status_filter() {
        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.listPurchaseOrders(eq(storeId), eq(userId), eq("PENDING"), eq(0), eq(10)))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        given()
                .queryParam("status", "PENDING")
                .when()
                .get("/api/v1/stores/" + storeId + "/purchase-orders")
                .then()
                .statusCode(200);

        verify(purchaseOrderService).listPurchaseOrders(
                eq(storeId), eq(userId), eq("PENDING"), eq(0), eq(10));
    }

    // GET DETAIL
    @Test
    @TestSecurity(user = "manager@test.com", roles = "MANAGER")
    void should_get_purchase_order_detail_success() {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.id = orderId;

        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.getPurchaseOrder(eq(storeId), eq(orderId), eq(userId)))
                .thenReturn(response);

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/purchase-orders/" + orderId)
                .then()
                .statusCode(200);
    }

    // UPDATE STATUS
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_update_purchase_order_status_to_confirmed() {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.id = orderId;
        response.status = "CONFIRMED";

        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.updateStatus(eq(storeId), eq(orderId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "CONFIRMED" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/purchase-orders/" + orderId + "/status")
                .then()
                .statusCode(200)
                .body("data.status", equalTo("CONFIRMED"));
    }

    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_update_purchase_order_status_to_received() {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.id = orderId;
        response.status = "RECEIVED";

        when(currentUser.getUserId()).thenReturn(userId);
        when(purchaseOrderService.updateStatus(eq(storeId), eq(orderId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "RECEIVED" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/purchase-orders/" + orderId + "/status")
                .then()
                .statusCode(200)
                .body("data.status", equalTo("RECEIVED"));
    }

    @Test
    void should_failed_update_status_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "CONFIRMED" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/purchase-orders/" + orderId + "/status")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "staff@test.com", roles = "STAFF")
    void should_failed_update_status_forbidden_for_staff() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "CONFIRMED" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/purchase-orders/" + orderId + "/status")
                .then()
                .statusCode(403);
    }
}