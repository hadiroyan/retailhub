package org.hadiroyan.retailhub.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hadiroyan.retailhub.exception.BadRequestException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileStorageService {

    private static final Logger LOG = Logger.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @ConfigProperty(name = "app.upload.dir", defaultValue = "./uploads")
    String uploadDir;

    // =========================================================================
    // Upload
    // =========================================================================

    public String uploadProductImage(UUID storeId, FileUpload file) {

        LOG.debugf("action=FILE_UPLOAD_START storeId=%s filename=%s", storeId, file.fileName());

        validateFile(file.contentType(), file.size());

        try (InputStream inputStream = Files.newInputStream(file.uploadedFile())) {
            String extension = getExtension(file.fileName(), file.contentType());
            String filename = UUID.randomUUID().toString() + "." + extension;
            String subDir = "products/" + storeId;

            Path dirPath = Paths.get(uploadDir, subDir);
            Path filePath = dirPath.resolve(filename);

            Files.createDirectories(dirPath);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            LOG.infof("action=FILE_UPLOAD_SUCCESS path=%s", filePath);

            return "products/" + storeId + "/" + filename;
        } catch (IOException ioe) {
            LOG.errorf("action=FILE_UPLOAD_FAILED error=%s", ioe.getMessage());
            throw new RuntimeException("Failed to upload file");
        }

    }

    // =========================================================================
    // Delete
    // =========================================================================
    public void deleteFile(String storageKey) {
        if (storageKey == null || storageKey.isBlank())
            return;

        Path filePath = Paths.get(uploadDir, storageKey);

        try {
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                LOG.infof("action=FILE_DELETE_SUCCESS path=%s", filePath);
            } else {
                LOG.warnf("action=FILE_DELETE_NOT_FOUND path=%s", filePath);
            }

        } catch (IOException e) {
            LOG.errorf("action=FILE_DELETE_FAILED path=%s error=%s", filePath, e.getMessage());
        }
    }

    // =========================================================================
    // Serve
    // =========================================================================

    public Path resolveFilePath(String subPath) {
        return Paths.get(uploadDir, subPath);
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

    private String getExtension(String originalFileName, String contentType) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}