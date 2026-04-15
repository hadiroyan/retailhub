package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateCategoryRequest;
import org.hadiroyan.retailhub.dto.request.UpdateCategoryRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.CategoryResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.CategoryService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.jboss.logging.Logger;

import jakarta.annotation.security.PermitAll;
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

@Path("/api/v1/stores/{storeId}/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private static final Logger LOG = Logger.getLogger(CategoryResource.class);

    @Inject
    CategoryService categoryService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response createCategory(@PathParam("storeId") UUID storeId, @Valid CreateCategoryRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=CREATE_CATEGORY_REQUEST userId=%s storeId=%s name=%s",
                userId, storeId, request.name);

        CategoryResponse response = categoryService.createCategory(storeId, userId, request);

        LOG.infof("action=CREATE_CATEGORY_RESPONSE userId=%s storeId=%s categoryId=%s",
                userId, storeId, response.id);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Category created successfully", response))
                .build();
    }

    @GET
    @PermitAll
    public Response listCategories(
            @PathParam("storeId") UUID storeId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        LOG.debugf("action=LIST_CATEGORIES_REQUEST storeId=%s page=%d size=%d",
                storeId, page, size);

        PagedResponse<CategoryResponse> result = categoryService.listCategories(storeId, page, size);

        LOG.infof("action=LIST_CATEGORIES_RESPONSE storeId=%s total=%d page=%d size=%d",
                storeId, result.totalElements, page, size);

        return Response.ok(ApiResponse.success("Categories retrieved successfully", result)).build();
    }

    @GET
    @Path("/{slug}")
    @PermitAll
    public Response getCategoryBySlug(
            @PathParam("storeId") UUID storeId,
            @PathParam("slug") String slug) {

        LOG.debugf("action=GET_CATEGORY_BY_SLUG_REQUEST storeId=%s slug=%s",
                storeId, slug);

        CategoryResponse response = categoryService.getCategoryBySlug(storeId, slug);

        LOG.infof("action=GET_CATEGORY_BY_SLUG_RESPONSE storeId=%s categoryId=%s slug=%s",
                storeId, response.id, slug);
        return Response.ok(ApiResponse.success("Category retrieved successfully", response)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response updateCategory(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID categoryId,
            @Valid UpdateCategoryRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=UPDATE_CATEGORY_REQUEST userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);

        CategoryResponse response = categoryService.updateCategory(storeId, categoryId, userId, request);

        LOG.infof("action=UPDATE_CATEGORY_RESPONSE userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);
        return Response.ok(ApiResponse.success("Category updated successfully", response)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response deleteCategory(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID categoryId) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=DELETE_CATEGORY_REQUEST userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);

        categoryService.deleteCategory(storeId, categoryId, userId);

        LOG.infof("action=DELETE_CATEGORY_RESPONSE userId=%s storeId=%s categoryId=%s",
                userId, storeId, categoryId);
        return Response.ok(ApiResponse.success("Category deleted successfully")).build();
    }
}
