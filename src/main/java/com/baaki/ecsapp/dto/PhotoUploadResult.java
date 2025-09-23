package com.baaki.ecsapp.dto;

/**
 * DTO for photo upload results
 */
public class PhotoUploadResult {
    private boolean success;
    private String message;
    private Long photoId;

    private PhotoUploadResult(boolean success, String message, Long photoId) {
        this.success = success;
        this.message = message;
        this.photoId = photoId;
    }

    public static PhotoUploadResult success(Long photoId, String message) {
        return new PhotoUploadResult(true, message, photoId);
    }

    public static PhotoUploadResult error(String message) {
        return new PhotoUploadResult(false, message, null);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getPhotoId() { return photoId; }
}
