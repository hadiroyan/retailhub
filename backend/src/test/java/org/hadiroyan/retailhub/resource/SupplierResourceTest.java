package org.hadiroyan.retailhub.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.SupplierResponse;
import org.hadiroyan.retailhub.service.SupplierService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

@QuarkusTest
public class SupplierResourceTest {

    @InjectMock
    SupplierService supplierService;

    @InjectMock
    CurrentUserUtil currentUser;

    UUID storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID supplierId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    // CREATE
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_create_supplier_success() {
        SupplierResponse response = new SupplierResponse();
        response.id = supplierId;
        response.name = "PT Supplier ABC";

        when(currentUser.getUserId()).thenReturn(userId);
        when(supplierService.createSupplier(eq(storeId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "PT Supplier ABC",
                            "phone": "08123456789"
                        }
                        """)
                .when()
                .post("/api/v1/stores/" + storeId + "/suppliers")
                .then()
                .statusCode(201)
                .body("data.name", equalTo("PT Supplier ABC"));
    }

    @Test
    void should_failed_create_supplier_unauthorized() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/v1/stores/" + storeId + "/suppliers")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "manager@test.com", roles = "MANAGER")
    void should_failed_create_supplier_forbidden_for_manager() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/v1/stores/" + storeId + "/suppliers")
                .then()
                .statusCode(403);
    }

    // LIST
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_list_suppliers_success() {
        SupplierResponse supplier = new SupplierResponse();
        supplier.id = supplierId;
        supplier.name = "PT Supplier ABC";

        when(currentUser.getUserId()).thenReturn(userId);
        when(supplierService.listSuppliers(eq(storeId), eq(userId), eq(0), eq(10)))
                .thenReturn(new PagedResponse<>(List.of(supplier), 0, 10, 1));

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/suppliers")
                .then()
                .statusCode(200)
                .body("data.content", hasSize(1));
    }

    @Test
    @TestSecurity(user = "staff@test.com", roles = "STAFF")
    void should_failed_list_suppliers_forbidden_for_staff() {
        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/suppliers")
                .then()
                .statusCode(403);
    }

    // GET DETAIL
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_get_supplier_success() {
        SupplierResponse response = new SupplierResponse();
        response.id = supplierId;

        when(currentUser.getUserId()).thenReturn(userId);
        when(supplierService.getSupplier(eq(storeId), eq(supplierId), eq(userId)))
                .thenReturn(response);

        given()
                .when()
                .get("/api/v1/stores/" + storeId + "/suppliers/" + supplierId)
                .then()
                .statusCode(200);
    }

    // UPDATE
    @Test
    @TestSecurity(user = "admin@test.com", roles = "ADMIN")
    void should_update_supplier_success() {
        SupplierResponse response = new SupplierResponse();
        response.id = supplierId;
        response.name = "PT Supplier Updated";

        when(currentUser.getUserId()).thenReturn(userId);
        when(supplierService.updateSupplier(eq(storeId), eq(supplierId), eq(userId), any()))
                .thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "PT Supplier Updated"
                        }
                        """)
                .when()
                .put("/api/v1/stores/" + storeId + "/suppliers/" + supplierId)
                .then()
                .statusCode(200)
                .body("data.name", equalTo("PT Supplier Updated"));
    }

    // DELETE
    @Test
    @TestSecurity(user = "owner@test.com", roles = "OWNER")
    void should_delete_supplier_success() {
        when(currentUser.getUserId()).thenReturn(userId);
        doNothing().when(supplierService).deleteSupplier(eq(storeId), eq(supplierId), eq(userId));

        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/suppliers/" + supplierId)
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "manager@test.com", roles = "MANAGER")
    void should_failed_delete_supplier_forbidden_for_manager() {
        given()
                .when()
                .delete("/api/v1/stores/" + storeId + "/suppliers/" + supplierId)
                .then()
                .statusCode(403);
    }
}