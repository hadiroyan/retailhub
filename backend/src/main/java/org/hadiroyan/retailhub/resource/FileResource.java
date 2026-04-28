package org.hadiroyan.retailhub.resource;

import java.io.IOException;
import java.nio.file.Files;

import org.hadiroyan.retailhub.service.FileStorageService;
import org.jboss.logging.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/files")
@PermitAll
public class FileResource {

    private static final Logger LOG = Logger.getLogger(FileResource.class);

    @Inject
    FileStorageService fileStorageService;

    @GET
    @Path("/products/{storeId}/{filename}")
    @Produces("image/*")
    public Response serveProductImage(
            @PathParam("storeId") String storeId,
            @PathParam("filename") String filename) {

        LOG.debugf("action=GET_IMAGE_PRODUCT_REQUEST storeId=%s filename=%s", storeId, filename);

        java.nio.file.Path filePath = fileStorageService.resolveFilePath("products/" + storeId + "/" + filename);

        if (!Files.exists(filePath)) {
            LOG.warnf("action=GET_IMAGE_PRODUCT_NOT_FOUND storeId=%s filename=%s", storeId, filename);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null)
                contentType = "image/jpeg";

            LOG.infof("action=GET_IMAGE_PRODUCT_RESPONSE storeId=%s filename=%s", storeId, filename);
            return Response.ok(filePath.toFile())
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "public, max-age=86400")
                    .build();
        } catch (IOException e) {
            LOG.errorf("action=GET_IMAGE_PRODUCT_ERROR message=%s storeId=%s filename=%s",
                    e.getMessage(),
                    storeId,
                    filename);
            return Response.serverError().build();
        }
    }
}