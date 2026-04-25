package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.PagedResponse;
import org.hadiroyan.retailhub.dto.response.ProductResponse;
import org.hadiroyan.retailhub.service.ProductService;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class ExploreResource {

    @Inject
    ProductService productService;

    @GET
    public Response listProducts(
            @QueryParam("name") String name,
            @QueryParam("storeId") UUID storeId,
            @QueryParam("categoryId") UUID categoryId,
            @QueryParam("sortByPrice") String sortByPrice,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        PagedResponse<ProductResponse> result = productService.listProductsGlobal(
                name, storeId, categoryId, sortByPrice, page, size);
        return Response.ok(ApiResponse.success("Products retrieved successfully", result)).build();
    }
}