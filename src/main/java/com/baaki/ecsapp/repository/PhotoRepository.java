package com.baaki.ecsapp.repository;

import com.baaki.ecsapp.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    // Find all photos ordered by upload date (newest first)
    @Query("SELECT p FROM Photo p ORDER BY p.uploadedAt DESC")
    List<Photo> findAllOrderByUploadedAtDesc();

    // Find photos by content type (e.g., for filtering by image type)
    List<Photo> findByContentTypeContaining(String contentType);
}