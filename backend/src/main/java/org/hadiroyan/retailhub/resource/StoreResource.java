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
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.UUID;

@Path("/api/v1/stores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StoreResource {

    private static final Logger LOG = Logger.getLogger(StoreResource.class);

    @Inject
    StoreService storeService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed("OWNER")
    public Response createStore(@Valid CreateStoreRequest request) {
        String email = currentUser.getEmail();
        LOG.debugf("action=CREATE_STORE_REQUEST email=%s storeName=%s",
                email, request.name);

        StoreResponse response = storeService.createStore(email, request);

        LOG.infof("action=CREATE_STORE_RESPONSE email=%s storeId=%s",
                email, response.id);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Store created successfully", response))
                .build();
    }

    @GET
    @PermitAll
    public Response listStores(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        LOG.infof("Get list store request");

        UUID userId = currentUser.getUserIdOptional().orElse(null);
        Set<String> roles = currentUser.getRoles();

        LOG.debugf("action=LIST_STORES_REQUEST userId=%s roles=%s page=%d size=%d",
                userId, roles, page, size);

        PagedResponse<StoreResponse> result = storeService.listsStores(userId, roles, page, size);

        LOG.infof("action=LIST_STORES_RESPONSE userId=%s total=%d page=%d size=%d",
                userId, result.totalElements, page, size);

        return Response.ok(ApiResponse.success("Stores retrieved successfully", result)).build();
    }

    @GET
    @Path("/{slug}")
    @PermitAll
    public Response getStore(@PathParam("slug") String slug) {
        LOG.infof("Get store by slug request");

        UUID userId = currentUser.getUserIdOptional().orElse(null);
        Set<String> roles = currentUser.getRoles();

        LOG.debugf("action=GET_STORE_BY_SLUG_REQUEST userId=%s slug=%s",
                userId, slug);

        StoreResponse store = storeService.getStoreBySlug(slug, userId, roles);

        LOG.infof("action=GET_STORE_BY_SLUG_RESPONSE userId=%s storeId=%s slug=%s",
                userId, store.id, slug);

        return Response.ok(ApiResponse.success("Store retrieved successfully", store)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "SUPER_ADMIN" })
    public Response updateStore(
            @PathParam("id") UUID storeId,
            @Valid UpdateStoreRequest request) {

        UUID userId = currentUser.getUserId();
        Set<String> roles = currentUser.getRoles();

        LOG.debugf("action=UPDATE_STORE_REQUEST userId=%s storeId=%s",
                userId, storeId);

        StoreResponse store = storeService.updateStore(storeId, userId, roles, request);

        LOG.infof("action=UPDATE_STORE_RESPONSE userId=%s storeId=%s",
                userId, storeId);

        return Response.ok(ApiResponse.success("Store updated successfully", store)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "SUPER_ADMIN" })
    public Response deleteStore(@PathParam("id") UUID storeId) {
        UUID userId = currentUser.getUserId();
        Set<String> roles = currentUser.getRoles();
        LOG.debugf("action=DELETE_STORE_REQUEST userId=%s storeId=%s",
                userId, storeId);

        storeService.deleteStore(storeId, userId, roles);

        LOG.infof("action=DELETE_STORE_RESPONSE userId=%s storeId=%s",
                userId, storeId);

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
        Set<String> roles = currentUser.getRoles();

        LOG.debugf("action=UPDATE_STORE_STATUS_REQUEST userId=%s storeId=%s status=%s",
                userId, storeId, request.status);

        StoreResponse store = storeService.updateStatus(storeId, userId, roles, request);

        LOG.infof("action=UPDATE_STORE_STATUS_RESPONSE userId=%s storeId=%s status=%s",
                userId, storeId, store.status);
        return Response.ok(ApiResponse.success("Store status updated successfully", store)).build();
    }

}
