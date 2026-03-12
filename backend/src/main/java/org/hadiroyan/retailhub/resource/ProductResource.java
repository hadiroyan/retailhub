package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.request.CreateProductRequest;
import org.hadiroyan.retailhub.dto.request.UpdateProductRequest;
import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.dto.response.ProductResponse;
import org.hadiroyan.retailhub.service.ProductService;
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

@Path("/api/v1/stores/{storeId}/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    ProductService productService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response createProduct(@PathParam("storeId") UUID storeId, CreateProductRequest request) {

        UUID userId = currentUser.getUserId();

        ProductDetailResponse response = productService.createProduct(storeId, userId, request);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Product created susccesfully", response))
                .build();
    }

    @GET
    @PermitAll
    public Response listProducts(
            @PathParam("storeId") UUID storeId,
            @QueryParam("name") String name,
            @QueryParam("categoryId") UUID categoryId,
            @QueryParam("sortByPrice") String sortByPrice,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        PagedResponse<ProductResponse> result = productService
                .listProducts(storeId, name, categoryId, sortByPrice, page, size);

        return Response.ok(ApiResponse.success("Product retrieved successfully", result)).build();
    }

    @GET
    @Path("/{sku}")
    @PermitAll
    public Response getProductBySku(
            @PathParam("storeId") UUID storeId,
            @PathParam("sku") String sku) {

        ProductResponse response = productService.getProductBySku(storeId, sku);
        return Response.ok(ApiResponse.success("Product retrieved successfully", response)).build();
    }

    // Internal endpoint — return costPrice and minStockLevel
    @GET
    @Path("/{sku}/detail")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response getProductDetail(
            @PathParam("storeId") UUID storeId,
            @PathParam("sku") String sku) {

        UUID userId = currentUser.getUserId();

        ProductDetailResponse response = productService.getProductDetail(storeId, sku, userId);
        return Response.ok(ApiResponse.success("Product retrieved successfully", response)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response updateProduct(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID productId,
            @Valid UpdateProductRequest request) {

        UUID userId = currentUser.getUserId();

        ProductDetailResponse response = productService.updateProduct(storeId, productId, userId, request);
        return Response.ok(ApiResponse.success("Product updated successfully", response)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response deleteProduct(
            @PathParam("storeId") UUID storeId,
            @PathParam("id") UUID productId) {

        UUID userId = currentUser.getUserId();

        productService.deleteProduct(storeId, productId, userId);
        return Response.ok(ApiResponse.success("Product deleted successfully")).build();
    }
}
