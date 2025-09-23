package com.baaki.ecsapp.controller;

import com.baaki.ecsapp.dto.PhotoUploadResult;
import com.baaki.ecsapp.dto.PhotoWithPresignedUrl;
import com.baaki.ecsapp.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PhotoController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    @Autowired
    private PhotoService photoService;

    /**
     * Display the photo gallery
     */
    @GetMapping("/gallery")
    public String gallery(Model model) {
        try {
            List<PhotoWithPresignedUrl> photos = photoService.getAllPhotosWithUrls();
            long photoCount = photoService.getPhotoCount();

            model.addAttribute("photos", photos);
            model.addAttribute("photoCount", photoCount);
            model.addAttribute("pageTitle", "Photo Gallery");

            logger.debug("Displaying gallery with {} photos", photos.size());
            return "gallery";

        } catch (Exception e) {
            logger.error("Error loading gallery: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to load photos. Please try again later.");
            return "gallery";
        }
    }

    /**
     * Display upload form
     */
    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("pageTitle", "Upload Photo");
        return "upload";
    }

    /**
     * Handle photo upload
     */
    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              @RequestParam(value = "description", required = false) String description,
                              RedirectAttributes redirectAttributes) {

        logger.info("Received upload request for file: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        try {
            PhotoUploadResult result = photoService.uploadPhoto(file, description);

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
                logger.info("Photo uploaded successfully with ID: {}", result.getPhotoId());
                return "redirect:/gallery";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
                return "redirect:/upload";
            }

        } catch (Exception e) {
            logger.error("Unexpected error during photo upload: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/upload";
        }
    }

    /**
     * REST API endpoint to get all photos (JSON)
     */
    @GetMapping("/api/photos")
    @ResponseBody
    public ResponseEntity<List<PhotoWithPresignedUrl>> getPhotos() {
        try {
            List<PhotoWithPresignedUrl> photos = photoService.getAllPhotosWithUrls();
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            logger.error("Error fetching photos via API: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * REST API endpoint to upload photo (JSON response)
     */
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadPhotoApi(@RequestParam("file") MultipartFile file,
                                                              @RequestParam(value = "description", required = false) String description) {

        Map<String, Object> response = new HashMap<>();

        try {
            PhotoUploadResult result = photoService.uploadPhoto(file, description);

            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                response.put("photoId", result.getPhotoId());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            logger.error("Error in photo upload API: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a photo
     */
    @PostMapping("/photos/{id}/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = photoService.deletePhoto(id);

            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Photo deleted successfully");
                logger.info("Photo with ID {} deleted successfully", id);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete photo");
                logger.warn("Failed to delete photo with ID {}", id);
            }

        } catch (Exception e) {
            logger.error("Error deleting photo with ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the photo");
        }

        return "redirect:/gallery";
    }

    /**
     * REST API endpoint to delete photo
     */
    @DeleteMapping("/api/photos/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePhotoApi(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = photoService.deletePhoto(id);

            response.put("success", deleted);
            response.put("message", deleted ? "Photo deleted successfully" : "Failed to delete photo");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in delete photo API for ID {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An error occurred while deleting the photo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            long photoCount = photoService.getPhotoCount();
            health.put("status", "healthy");
            health.put("photoCount", photoCount);
            health.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}