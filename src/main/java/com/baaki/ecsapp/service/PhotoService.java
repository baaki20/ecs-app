package com.baaki.ecsapp.service;

import com.baaki.ecsapp.dto.PhotoUploadResult;
import com.baaki.ecsapp.dto.PhotoWithPresignedUrl;
import com.baaki.ecsapp.model.Photo;
import com.baaki.ecsapp.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private S3Service s3Service;

    /**
     * Upload a new photo with validation
     */
    public PhotoUploadResult uploadPhoto(MultipartFile file, String description) {
        try {
            // Validate file
            validateFile(file);

            // Upload to S3
            String s3Key = s3Service.uploadFile(file, description);

            // Save metadata to database
            Photo photo = new Photo(
                    file.getOriginalFilename(),
                    s3Key,
                    description,
                    file.getContentType(),
                    file.getSize()
            );

            photo = photoRepository.save(photo);

            logger.info("Successfully uploaded photo with ID: {} and S3 key: {}", photo.getId(), s3Key);

            return PhotoUploadResult.success(photo.getId(), "Photo uploaded successfully");

        } catch (IllegalArgumentException e) {
            logger.warn("File validation failed: {}", e.getMessage());
            return PhotoUploadResult.error(e.getMessage());
        } catch (IOException e) {
            logger.error("Failed to upload photo: {}", e.getMessage(), e);
            return PhotoUploadResult.error("Failed to upload photo. Please try again.");
        } catch (Exception e) {
            logger.error("Unexpected error during photo upload: {}", e.getMessage(), e);
            return PhotoUploadResult.error("An unexpected error occurred. Please try again.");
        }
    }

    /**
     * Get all photos with presigned URLs for viewing
     */
    @Transactional(readOnly = true)
    public List<PhotoWithPresignedUrl> getAllPhotosWithUrls() {
        List<Photo> photos = photoRepository.findAllOrderByUploadedAtDesc();

        return photos.stream()
                .map(this::createPhotoWithPresignedUrl)
                .filter(photoWithUrl -> photoWithUrl.getPresignedUrl() != null) // Filter out photos with failed URL generation
                .collect(Collectors.toList());
    }

    /**
     * Get a specific photo with presigned URL
     */
    @Transactional(readOnly = true)
    public PhotoWithPresignedUrl getPhotoWithUrl(Long photoId) {
        return photoRepository.findById(photoId)
                .map(this::createPhotoWithPresignedUrl)
                .orElse(null);
    }

    /**
     * Delete a photo
     */
    public boolean deletePhoto(Long photoId) {
        try {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo == null) {
                logger.warn("Attempted to delete non-existent photo with ID: {}", photoId);
                return false;
            }

            // Delete from S3 first
            boolean s3Deleted = s3Service.deleteFile(photo.getS3Key());
            if (!s3Deleted) {
                logger.warn("Failed to delete photo from S3, but continuing with database deletion");
            }

            // Delete from database
            photoRepository.deleteById(photoId);

            logger.info("Successfully deleted photo with ID: {} and S3 key: {}", photoId, photo.getS3Key());
            return true;

        } catch (Exception e) {
            logger.error("Failed to delete photo with ID {}: {}", photoId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get photo count
     */
    @Transactional(readOnly = true)
    public long getPhotoCount() {
        return photoRepository.count();
    }

    private PhotoWithPresignedUrl createPhotoWithPresignedUrl(Photo photo) {
        String presignedUrl = s3Service.generatePresignedUrl(photo.getS3Key());
        return new PhotoWithPresignedUrl(photo, presignedUrl);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only image files (JPEG, PNG, GIF, WebP) are allowed");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }
}