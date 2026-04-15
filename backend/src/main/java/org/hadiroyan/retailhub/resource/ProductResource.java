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

@Path("/api/v1/stores/{storeId}/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final Logger LOG = Logger.getLogger(ProductResource.class);

    @Inject
    ProductService productService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response createProduct(@PathParam("storeId") UUID storeId, CreateProductRequest request) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=CREATE_PRODUCT_REQUEST userId=%s storeId=%s sku=%s name=%s",
                userId, storeId, request.sku, request.name);

        ProductDetailResponse response = productService.createProduct(storeId, userId, request);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Product created successfully", response))
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

        LOG.debugf("action=LIST_PRODUCTS_REQUEST storeId=%s name=%s categoryId=%s page=%d size=%d",
                storeId, name, categoryId, page, size);

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

        LOG.debugf("action=GET_PRODUCT_BY_SKU_REQUEST storeId=%s sku=%s", storeId, sku);

        ProductResponse response = productService.getProductBySku(storeId, sku);
        return Response.ok(ApiResponse.success("Product retrieved successfully", response)).build();
    }

    @GET
    @Path("/{sku}/detail")
    @RolesAllowed({ "OWNER", "ADMIN", "MANAGER" })
    public Response getProductDetail(
            @PathParam("storeId") UUID storeId,
            @PathParam("sku") String sku) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=GET_PRODUCT_DETAIL_REQUEST userId=%s storeId=%s sku=%s",
                userId, storeId, sku);

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
        LOG.debugf("action=UPDATE_PRODUCT_REQUEST userId=%s storeId=%s productId=%s",
                userId, storeId, productId);

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
        LOG.debugf("action=DELETE_PRODUCT_REQUEST userId=%s storeId=%s productId=%s",
                userId, storeId, productId);

        productService.deleteProduct(storeId, productId, userId);
        return Response.ok(ApiResponse.success("Product deleted successfully")).build();
    }
}
