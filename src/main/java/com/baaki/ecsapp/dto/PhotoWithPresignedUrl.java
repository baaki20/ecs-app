package com.baaki.ecsapp.dto;

import com.baaki.ecsapp.model.Photo;
import java.time.LocalDateTime;

/**
 * DTO for transferring photo data with presigned URL
 */
public class PhotoWithPresignedUrl {
    private Long id;
    private String filename;
    private String description;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String presignedUrl;

    public PhotoWithPresignedUrl(Photo photo, String presignedUrl) {
        this.id = photo.getId();
        this.filename = photo.getFilename();
        this.description = photo.getDescription();
        this.contentType = photo.getContentType();
        this.fileSize = photo.getFileSize();
        this.uploadedAt = photo.getUploadedAt();
        this.presignedUrl = presignedUrl;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getPresignedUrl() { return presignedUrl; }
    public void setPresignedUrl(String presignedUrl) { this.presignedUrl = presignedUrl; }

    /**
     * Format file size for display
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";

        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}