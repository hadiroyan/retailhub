package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateOrderRequest;
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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("CUSTOMER")
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    @Inject
    OrderService orderService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    public Response createOrder(@Valid CreateOrderRequest request) {
        String email = currentUser.getEmail();
        LOG.debugf("action=CREATE_ORDER_REQUEST email=%s storeId=%s", email, request.storeId);

        OrderResponse response = orderService.createOrder(email, request);

        LOG.infof("action=CREATE_ORDER_RESPONSE email=%s orderId=%s orderNumber=%s",
                email, response.id, response.orderNumber);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Order created successfully", response))
                .build();
    }

    @GET
    public Response listOrders(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        UUID customerId = currentUser.getUserId();
        LOG.debugf("action=LIST_ORDERS_REQUEST customerId=%s page=%d size=%d",
                customerId, page, size);

        PagedResponse<OrderResponse> result = orderService.listCustomerOrders(customerId, page, size);

        LOG.infof("action=LIST_ORDERS_RESPONSE customerId=%s page=%d size=%d",
                customerId, page, size);

        return Response.ok(ApiResponse.success("Orders retrieved successfully", result)).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") UUID orderId) {
        UUID customerId = currentUser.getUserId();
        LOG.debugf("action=GET_ORDER_REQUEST customerId=%s orderId=%s", customerId, orderId);

        OrderResponse response = orderService.getOrderDetail(orderId, customerId, true);
        LOG.infof("action=GET_ORDER_RESPONSE customerId=%s orderId=%s", customerId, orderId);

        return Response.ok(ApiResponse.success("Order retrieved successfully", response)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response cancelOrder(@PathParam("id") UUID orderId) {
        UUID customerId = currentUser.getUserId();
        LOG.debugf("action=CANCEL_ORDER_REQUEST customerId=%s orderId=%s", customerId, orderId);

        OrderResponse response = orderService.cancelOrder(orderId, customerId);

        LOG.infof("action=CANCEL_ORDER_RESPONSE customerId=%s orderId=%s", customerId, orderId);

        return Response.ok(ApiResponse.success("Order cancelled successfully", response)).build();
    }
}