package org.edunex.courseservice.controller;

import org.edunex.courseservice.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller specifically for file-related operations
 * This provides endpoints for direct file downloads that bypass CloudFront
 * when necessary (e.g., for administrative purposes)
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private S3Service s3Service;
    
    /**
     * Direct download of a file from S3, bypassing CloudFront
     * This can be useful for administrative purposes or when 
     * needing original files without CloudFront caching
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String objectKey) {
        return s3Service.downloadFile(objectKey);
    }
}