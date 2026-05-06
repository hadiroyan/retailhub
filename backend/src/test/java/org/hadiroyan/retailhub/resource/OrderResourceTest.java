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
public class OrderResourceTest {

    @InjectMock
    OrderService orderService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID orderId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // CREATE ORDER
    @Test
    @TestSecurity(user = "customer@test.com", roles = "CUSTOMER")
    void should_create_order_success() {
        OrderResponse response = new OrderResponse();
        response.id = orderId;
        response.orderNumber = "TRX-20260501120000-ABCD";
        response.totalAmount = BigDecimal.valueOf(100000);

        when(currentUser.getEmail()).thenReturn("customer@test.com");
        when(orderService.createOrder(eq("customer@test.com"), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "storeId": "%s",
                            "recipientName": "John Doe",
                            "phone": "08123456789",
                            "shippingAddress": "Jl. Sudirman No. 1",
                            "items": [
                                { "productId": "%s", "quantity": 1 }
                            ]
                        }
                        """.formatted(storeId, UUID.randomUUID()))
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("data.orderNumber", equalTo("TRX-20260501120000-ABCD"));
    }

    @Test
    void should_failed_create_order_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_failed_create_order_forbidden_for_owner() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(403);
    }

    // LIST ORDERS
    @Test
    @TestSecurity(user = "customer@test.com", roles = "CUSTOMER")
    void should_list_orders_success() {
        OrderResponse order = new OrderResponse();
        order.id = orderId;

        when(currentUser.getUserId()).thenReturn(customerId);
        when(orderService.listCustomerOrders(customerId, 0, 10))
                .thenReturn(new PagedResponse<>(List.of(order), 0, 10, 1));

        given()
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("data.content", hasSize(1));
    }

    // GET ORDER DETAIL
    @Test
    @TestSecurity(user = "customer@test.com", roles = "CUSTOMER")
    void should_get_order_detail_success() {
        OrderResponse response = new OrderResponse();
        response.id = orderId;

        when(currentUser.getUserId()).thenReturn(customerId);
        when(orderService.getOrderDetail(orderId, customerId, true)).thenReturn(response);

        given()
                .when()
                .get("/api/v1/orders/" + orderId)
                .then()
                .statusCode(200);
    }

    // CANCEL ORDER
    @Test
    @TestSecurity(user = "customer@test.com", roles = "CUSTOMER")
    void should_cancel_order_success() {
        OrderResponse response = new OrderResponse();
        response.id = orderId;
        response.status = "CANCELLED";

        when(currentUser.getUserId()).thenReturn(customerId);
        when(orderService.cancelOrder(orderId, customerId)).thenReturn(response);

        given()
                .when()
                .delete("/api/v1/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("data.status", equalTo("CANCELLED"));
    }

    @Test
    void should_failed_cancel_order_unauthorized() {
        given()
                .when()
                .delete("/api/v1/orders/" + orderId)
                .then()
                .statusCode(401);
    }
}