package org.hadiroyan.retailhub.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.OrderResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.OrderService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

@QuarkusTest
public class StoreOrderResourceTest {

    @InjectMock
    OrderService orderService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID requesterId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID orderId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // LIST STORE ORDERS
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_list_store_orders_success() {
        OrderResponse order = new OrderResponse();
        order.id = orderId;
        order.status = "PENDING";

        when(currentUser.getUserId()).thenReturn(requesterId);
        when(orderService.listStoreOrders(storeId, requesterId, null, 0, 10))
                .thenReturn(new PagedResponse<>(List.of(order), 0, 10, 1));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/orders")
                .then()
                .statusCode(200)
                .body("data.content", hasSize(1));
    }

    @Test
    @TestSecurity(user = "manager@test.com", roles = "MANAGER")
    void should_list_store_orders_with_status_filter() {
        when(currentUser.getUserId()).thenReturn(requesterId);
        when(orderService.listStoreOrders(storeId, requesterId, "PENDING", 0, 10))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        given()
                .queryParam("status", "PENDING")
                .when()
                .get("/api/v1/stores/" + storeId + "/orders")
                .then()
                .statusCode(200);

        verify(orderService).listStoreOrders(storeId, requesterId, "PENDING", 0, 10);
    }

    @Test
    void should_failed_list_store_orders_unauthorized() {
        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/orders")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "customer@test.com", roles = "CUSTOMER")
    void should_failed_list_store_orders_forbidden_for_customer() {
        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/orders")
                .then()
                .statusCode(403);
    }

    // GET ORDER DETAIL
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_get_store_order_detail_success() {
        OrderResponse response = new OrderResponse();
        response.id = orderId;
        response.totalAmount = BigDecimal.valueOf(100000);

        when(currentUser.getUserId()).thenReturn(requesterId);
        when(orderService.getOrderDetail(orderId, requesterId, false)).thenReturn(response);

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/orders/" + orderId)
                .then()
                .statusCode(200);
    }

    // UPDATE ORDER STATUS
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_update_order_status_success() {
        OrderResponse response = new OrderResponse();
        response.id = orderId;
        response.status = "PROCESSING";

        when(currentUser.getUserId()).thenReturn(requesterId);
        when(orderService.updateOrderStatus(eq(orderId), eq(requesterId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "PROCESSING" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/orders/" + orderId + "/status")
                .then()
                .statusCode(200)
                .body("data.status", equalTo("PROCESSING"));
    }

    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_update_order_status_shipped_with_tracking_number() {
        OrderResponse response = new OrderResponse();
        response.id = orderId;
        response.status = "SHIPPED";
        response.trackingNumber = "JNE-12345";

        when(currentUser.getUserId()).thenReturn(requesterId);
        when(orderService.updateOrderStatus(eq(orderId), eq(requesterId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "SHIPPED", "trackingNumber": "JNE-12345" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/orders/" + orderId + "/status")
                .then()
                .statusCode(200)
                .body("data.trackingNumber", equalTo("JNE-12345"));
    }

    @Test
    void should_failed_update_status_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "PROCESSING" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/orders/" + orderId + "/status")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "staff@test.com", roles = "STAFF")
    void should_list_store_orders_success_for_staff() {
        when(currentUser.getUserId()).thenReturn(requesterId);
        when(orderService.listStoreOrders(storeId, requesterId, null, 0, 10))
                .thenReturn(new PagedResponse<>(List.of(), 0, 10, 0));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/orders")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "staff@test.com", roles = "STAFF")
    void should_failed_update_status_forbidden_for_staff() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        { "status": "PROCESSING" }
                        """)
                .when()
                .patch("/api/v1/stores/" + storeId + "/orders/" + orderId + "/status")
                .then()
                .statusCode(403);
    }
}