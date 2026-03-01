package org.hadiroyan.retailhub.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hadiroyan.retailhub.dto.request.CreateStoreRequest;
import org.hadiroyan.retailhub.dto.request.UpdateStoreRequest;
import org.hadiroyan.retailhub.dto.request.UpdateStoreStatusRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.StoreResponse;
import org.hadiroyan.retailhub.service.StoreService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;

import java.util.Set;
import java.util.UUID;

@Path("/api/v1/stores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {

    @Inject
    StoreService storeService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed("OWNER")
    public Response createStore(@Valid CreateStoreRequest request) {
        String email = currentUser.getEmail();

        StoreResponse response = storeService.createStore(email, request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Store created successfully", response))
                .build();
    }

    @GET
    @PermitAll
    public Response listStores(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        UUID userId = currentUser.getUserIdOptional().orElse(null);
        Set<String> userRole = currentUser.getRoles();

        PagedResponse<StoreResponse> result = storeService.listsStores(userId, userRole, page, size);
        return Response.ok(ApiResponse.success("Stores retrieved successfully", result)).build();
    }

    @GET
    @Path("/{slug}")
    @PermitAll
    public Response getStore(@PathParam("slug") String slug) {

        UUID userId = currentUser.getUserIdOptional().orElse(null);
        Set<String> userRole = currentUser.getRoles();

        StoreResponse store = storeService.getStoreBySlug(slug, userId, userRole);
        return Response.ok(ApiResponse.success("Store retrieved successfully", store)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "SUPER_ADMIN" })
    public Response updateStore(
            @PathParam("id") UUID storeId,
            @Valid UpdateStoreRequest request) {

        UUID userId = currentUser.getUserId();
        Set<String> userRole = currentUser.getRoles();

        StoreResponse store = storeService.updateStore(storeId, userId, userRole, request);

        return Response.ok(ApiResponse.success("Store updated successfully", store)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "SUPER_ADMIN" })
    public Response deleteStore(@PathParam("id") UUID storeId) {
        UUID userId = currentUser.getUserId();
        Set<String> userRole = currentUser.getRoles();

        storeService.deleteStore(storeId, userId, userRole);
        return Response.ok(ApiResponse.success("Store deleted successfully")).build();
    }

    // Update status (SUPER_ADMIN only for suspend)
    @PATCH
    @Path("/{id}/status")
    @RolesAllowed({ "OWNER", "SUPER_ADMIN" })
    public Response updateStatus(
            @PathParam("id") UUID storeId,
            @Valid UpdateStoreStatusRequest request) {

        UUID userId = currentUser.getUserId();
        Set<String> userRole = currentUser.getRoles();

        StoreResponse store = storeService.updateStatus(storeId, userId, userRole, request);
        return Response.ok(ApiResponse.success("Store status updated successfully", store)).build();
    }

}
