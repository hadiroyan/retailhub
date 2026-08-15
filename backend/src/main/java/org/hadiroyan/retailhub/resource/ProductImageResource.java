package org.hadiroyan.retailhub.resource;

import java.util.UUID;

import org.hadiroyan.retailhub.dto.response.ApiResponse;
import org.hadiroyan.retailhub.dto.response.ProductDetailResponse;
import org.hadiroyan.retailhub.service.ProductImageService;
import org.hadiroyan.retailhub.utils.CurrentUserUtil;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/stores/{storeId}/products/{productId}/images")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({ "OWNER", "ADMIN", "MANAGER", "STAFF" })
public class ProductImageResource {

    private static final Logger LOG = Logger.getLogger(ProductImageResource.class);

    @Inject
    ProductImageService productImageService;

    @Inject
    CurrentUserUtil currentUser;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImage(
            @PathParam("storeId") UUID storeId,
            @PathParam("productId") UUID productId,
            @RestForm("image") FileUpload file) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=UPLOAD_IMAGE_PRODUCT_REQUEST userId=%s", userId);

        ProductDetailResponse response = productImageService.uploadImage(
                storeId, productId, userId, file);

        LOG.infof("action=UPLOAD_IMAGE_PRODUCT_RESPONSE status=%s userId=%s", response.status, userId);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created("Image uploaded successfully", response))
                .build();
    }

    @DELETE
    public Response deleteImage(
            @PathParam("storeId") UUID storeId,
            @PathParam("productId") UUID productId,
            @QueryParam("publicId") String publicId) {

        UUID userId = currentUser.getUserId();
        LOG.debugf("action=DELETE_IMAGE_PRODUCT_REQUEST userId=%s", userId);

        ProductDetailResponse response = productImageService.deleteImage(storeId, productId, userId, publicId);

        LOG.infof("action=DELETE_IMAGE_PRODUCT_RESPONSE status=%s userId=%s", response.status, userId);
        return Response.ok(ApiResponse.success("Image deleted successfully", response)).build();
    }
}