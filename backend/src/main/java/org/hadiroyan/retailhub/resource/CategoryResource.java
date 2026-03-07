package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateCategoryRequest;
import org.hadiroyan.retailhub.dto.request.UpdateCategoryRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.CategoryResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.service.CategoryService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;

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

    @Inject
    CategoryService categoryService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response createCategory(@PathParam("storeId") UUID storeId, @Valid CreateCategoryRequest request) {

        UUID userId = currentUser.getUserId();

        CategoryResponse response = categoryService.createCategory(storeId, userId, request);
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

        PagedResponse<CategoryResponse> result = categoryService.listCategories(storeId, page, size);
        return Response.ok(ApiResponse.success("Categories retrieved successfully", result)).build();
    }

    @GET
    @Path("/{slug}")
    @PermitAll
    public Response getCategoryBySlug(
            @PathParam("storeId") UUID storeId,
            @PathParam("slug") String slug) {

        CategoryResponse response = categoryService.getCategoryBySlug(storeId, slug);
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

        CategoryResponse response = categoryService.updateCategory(storeId, categoryId, userId, request);
        return Response.ok(ApiResponse.success("Category updated successfully", response)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response deleteCategory(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID categoryId) {

        UUID userId = currentUser.getUserId();

        categoryService.deleteCategory(storeId, categoryId, userId);
        return Response.ok(ApiResponse.success("Category deleted successfully")).build();
    }
}
