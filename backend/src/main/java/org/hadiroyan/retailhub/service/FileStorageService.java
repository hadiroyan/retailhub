package org.hadiroyan.retailhub.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FileStorageService {

    private static final Logger LOG = Logger.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Inject
    Cloudinary cloudinary;

    @ConfigProperty(name = "cloudinary.folder-prefix", defaultValue = "products")
    String folderPrefix;

    // =========================================================================
    // Upload
    // =========================================================================

    public String uploadProductImage(UUID storeId, FileUpload file) {

        LOG.debugf("action=FILE_UPLOAD_START storeId=%s filename=%s", storeId, file.fileName());

        validateFile(file.contentType(), file.size());

        try {
            String folder = folderPrefix + "/" + storeId;
            String publicId = UUID.randomUUID().toString();

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.uploadedFile().toFile(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", publicId,
                            "resource_type", "image",
                            "overwrite", false));

            String returnedPublicId = (String) uploadResult.get("public_id");

            LOG.infof("action=FILE_UPLOAD_SUCCESS publicId=%s", returnedPublicId);

            return returnedPublicId;
        } catch (IOException ioe) {
            LOG.errorf("action=FILE_UPLOAD_FAILED error=%s", ioe.getMessage());
            throw new RuntimeException("Failed to upload file");
        }
    }

    // =========================================================================
    // Delete
    // =========================================================================

    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank())
            return;

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");

            if ("ok".equals(status)) {
                LOG.infof("action=FILE_DELETE_SUCCESS publicId=%s", publicId);
            } else {
                LOG.warnf("action=FILE_DELETE_NOT_FOUND publicId=%s status=%s", publicId, status);
            }
        } catch (IOException e) {
            LOG.errorf("action=FILE_DELETE_FAILED publicId=%s error=%s", publicId, e.getMessage());
        }
    }

    // =========================================================================
    // URL Generation — dipakai ProductMapper
    // =========================================================================

    public String buildImageUrl(String publicId) {
        if (publicId == null || publicId.isBlank())
            return null;

        return cloudinary.url()
                .transformation(new Transformation<>().fetchFormat("auto").quality("auto"))
                .secure(true)
                .generate(publicId);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void validateFile(String contentType, long fileSize) {
        if (fileSize == 0) {
            LOG.warnf("action=VALIDATE_FILE_SIZE_EMPTY size=%s", fileSize);
            throw new BadRequestException("File is empty");
        }

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            LOG.warnf("action=VALIDATE_FILE_FAILED message=%s content-type=%s file-size=%d",
                    "Invalid file type",
                    contentType,
                    fileSize);
            throw new BadRequestException("Invalid file type. Allowed: JPEG, PNG, WEBP");
        }
        if (fileSize > MAX_FILE_SIZE) {
            LOG.warnf("action=VALIDATE_FILE_FAILED_SIZE_LIMIT content-type=%s file-size=%d", contentType, fileSize);
            throw new BadRequestException("File size exceeds 5MB limit");
        }
    }

}