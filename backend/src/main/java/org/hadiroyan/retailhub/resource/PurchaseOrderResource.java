package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreatePurchaseOrderRequest;
import org.hadiroyan.retailhub.dto.request.UpdatePurchaseOrderStatusRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.PurchaseOrderResponse;
import org.hadiroyan.retailhub.service.PurchaseOrderService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.jboss.logging.Logger;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/stores/{storeId}/purchase-orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PurchaseOrderResource {

    private static final Logger LOG = Logger.getLogger(PurchaseOrderResource.class);

    @Inject
    PurchaseOrderService purchaseOrderService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response createPurchaseOrder(
            @PathParam("storeId") UUID storeId,
            @Valid CreatePurchaseOrderRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=CREATE_PO_REQUEST userId=%s storeId=%s supplierId=%s",
                userId, storeId, request.supplierId);

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(
                storeId, userId, request);

        LOG.infof("action=CREATE_PO_RESPONSE userId=%s storeId=%s orderId=%s orderNumber=%s",
                userId, storeId, response.id, response.orderNumber);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Purchase order created successfully", response))
                .build();
    }

    @GET
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER", "STAFF" })
    public Response listPurchaseOrders(
            @PathParam("storeId") UUID storeId,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=LIST_PO_REQUEST userId=%s storeId=%s status=%s", userId, storeId, status);

        PagedResponse<PurchaseOrderResponse> result = purchaseOrderService.listPurchaseOrders(
                storeId, userId, status, page, size);

        LOG.infof("action=LIST_PO_RESPONSE userId=%s storeId=%s status=%s", userId, storeId, status);

        return Response.ok(ApiResponse.success("Purchase orders retrieved successfully", result)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER", "STAFF" })
    public Response getPurchaseOrder(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID orderId) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=GET_PO_REQUEST userId=%s storeId=%s orderId=%s",
                userId, storeId, orderId);

        PurchaseOrderResponse response = purchaseOrderService.getPurchaseOrder(
                storeId, orderId, userId);

        LOG.infof("action=GET_PO_RESPONSE userId=%s storeId=%s orderId=%s",
                userId, storeId, orderId);

        return Response.ok(ApiResponse.success("Purchase order retrieved successfully", response)).build();
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response updateStatus(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID orderId,
            @Valid UpdatePurchaseOrderStatusRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=UPDATE_PO_STATUS_REQUEST userId=%s storeId=%s orderId=%s status=%s",
                userId, storeId, orderId, request.status);

        PurchaseOrderResponse response = purchaseOrderService.updateStatus(
                storeId, orderId, userId, request);

        LOG.infof("action=UPDATE_PO_STATUS_RESPONSE userId=%s storeId=%s orderId=%s status=%s",
                userId, storeId, orderId, request.status);

        return Response.ok(ApiResponse.success("Purchase order status updated successfully", response))
                .build();
    }
}