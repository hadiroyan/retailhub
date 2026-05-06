package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.UpdateOrderStatusRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.OrderResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.OrderService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.jboss.logging.Logger;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/stores/{storeId}/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreOrderResource {

    private static final Logger LOG = Logger.getLogger(StoreOrderResource.class);

    @Inject
    OrderService orderService;

    @Inject
    CurrentUserUtil currentUser;

    @GET
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER", "STAFF" })
    public Response listOrders(
            @PathParam("storeId") UUID storeId,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        UUID requesterId = currentUser.getUserId();
        LOG.debugf("action=LIST_STORE_ORDERS_REQUEST requesterId=%s storeId=%s status=%s",
                requesterId, storeId, status);

        PagedResponse<OrderResponse> result = orderService.listStoreOrders(
                storeId, requesterId, status, page, size);

        LOG.infof("action=LIST_STORE_ORDERS_RESPONSE requesterId=%s storeId=%s status=%s",
                requesterId, storeId, status);

        return Response.ok(ApiResponse.success("Orders retrieved successfully", result)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER", "STAFF" })
    public Response getOrder(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID orderId) {

        UUID requesterId = currentUser.getUserId();
        LOG.debugf("action=GET_STORE_ORDER_REQUEST requesterId=%s storeId=%s orderId=%s",
                requesterId, storeId, orderId);

        OrderResponse response = orderService.getOrderDetail(orderId, requesterId, false);

        LOG.infof("action=GET_STORE_ORDER_RESPONSE requesterId=%s storeId=%s orderId=%s",
                requesterId, storeId, orderId);

        return Response.ok(ApiResponse.success("Order retrieved successfully", response)).build();
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response updateStatus(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID orderId,
            @Valid UpdateOrderStatusRequest request) {

        UUID requesterId = currentUser.getUserId();
        LOG.debugf("action=UPDATE_ORDER_STATUS_REQUEST requesterId=%s storeId=%s orderId=%s status=%s",
                requesterId, storeId, orderId, request.status);

        OrderResponse response = orderService.updateOrderStatus(orderId, requesterId, request);

        LOG.infof("action=UPDATE_ORDER_STATUS_RESPONSE requesterId=%s orderId=%s status=%s",
                requesterId, orderId, request.status);

        return Response.ok(ApiResponse.success("Order status updated successfully", response)).build();
    }
}