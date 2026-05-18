package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateSupplierRequest;
import org.hadiroyan.retailhub.dto.request.UpdateSupplierRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.SupplierResponse;
import org.hadiroyan.retailhub.service.SupplierService;
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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/stores/{storeId}/suppliers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SupplierResource {

    private static final Logger LOG = Logger.getLogger(SupplierResource.class);

    @Inject
    SupplierService supplierService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "OWNER", "ADMIN" })
    public Response createSupplier(
            @PathParam("storeId") UUID storeId,
            @Valid CreateSupplierRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=CREATE_SUPPLIER_REQUEST userId=%s storeId=%s", userId, storeId);

        SupplierResponse response = supplierService.createSupplier(storeId, userId, request);

        LOG.infof("action=CREATE_SUPPLIER_RESPONSE userId=%s storeId=%s supplierId=%s",
                userId, storeId, response.id);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Supplier created successfully", response))
                .build();
    }

    @GET
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response listSuppliers(
            @PathParam("storeId") UUID storeId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=LIST_SUPPLIERS_REQUEST userId=%s storeId=%s", userId, storeId);

        PagedResponse<SupplierResponse> result = supplierService.listSuppliers(
                storeId, userId, page, size);

        LOG.infof("action=LIST_SUPPLIERS_RESPONSE userId=%s storeId=%s", userId, storeId);

        return Response.ok(ApiResponse.success("Suppliers retrieved successfully", result)).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response getSupplier(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID supplierId) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=GET_SUPPLIER_REQUEST userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        SupplierResponse response = supplierService.getSupplier(storeId, supplierId, userId);

        LOG.infof("action=GET_SUPPLIER_RESPONSE userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        return Response.ok(ApiResponse.success("Supplier retrieved successfully", response)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN" })
    public Response updateSupplier(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID supplierId,
            @Valid UpdateSupplierRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=UPDATE_SUPPLIER_REQUEST userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        SupplierResponse response = supplierService.updateSupplier(
                storeId, supplierId, userId, request);

        LOG.infof("action=UPDATE_SUPPLIER_RESPONSE userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        return Response.ok(ApiResponse.success("Supplier updated successfully", response)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN" })
    public Response deleteSupplier(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID supplierId) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=DELETE_SUPPLIER_REQUEST userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        supplierService.deleteSupplier(storeId, supplierId, userId);

        LOG.infof("action=DELETE_SUPPLIER_RESPONSE userId=%s storeId=%s supplierId=%s",
                userId, storeId, supplierId);

        return Response.ok(ApiResponse.success("Supplier deleted successfully")).build();
    }
}