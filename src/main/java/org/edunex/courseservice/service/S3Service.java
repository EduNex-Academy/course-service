package org.edunex.courseservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;
    
    @Value("${aws.cloudfront.domain-name}")
    private String cloudfrontDomain;


    /**
     * Upload file to S3 bucket
     * @param file The file to upload
     * @param moduleId The module ID to associate with the file (used in the object key)
     * @return The generated S3 object key for the uploaded file
     */
    public String uploadFile(MultipartFile file, Long moduleId) {
        try {
            String contentType = file.getContentType();
            String extension = getExtensionFromContentType(contentType);
            
            // Generate a unique key for the object in S3
            String objectKey = "module-" + moduleId + "/" + UUID.randomUUID() + "." + extension;
            
            // Create metadata for the file
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", contentType);
            metadata.put("module-id", moduleId.toString());
            
            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            return objectKey;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 error: " + e.getMessage());
        }
    }

    /**
     * Download file from S3 bucket
     * @param objectKey The S3 object key for the file
     * @return ResponseEntity with the file content
     */
    public ResponseEntity<InputStreamResource> downloadFile(String objectKey) {
        try {
            // Get object request
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            
            // Get the object from S3
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            GetObjectResponse objectResponse = s3Object.response();
            
            // Get content type from metadata
            String contentType = objectResponse.contentType();
            
            // Set up the response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            
            // Extract filename from object key
            String filename = objectKey.substring(objectKey.lastIndexOf("/") + 1);
            headers.setContentDispositionFormData("attachment", filename);
            
            // Return the file as a streaming response
            return new ResponseEntity<>(
                new InputStreamResource(s3Object),
                headers,
                HttpStatus.OK
            );
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 error: " + e.getMessage());
        }
    }

    /**
     * Delete file from S3 bucket
     * @param objectKey The S3 object key for the file
     */
    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }
    
    /**
     * Determine if the file exists in the S3 bucket
     * @param objectKey The S3 object key to check
     * @return true if the object exists, false otherwise
     */
    public boolean doesFileExist(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
    
    /**
     * Get file extension from content type
     * @param contentType The content type
     * @return The file extension
     */
    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return "bin"; // Default extension for unknown content type
        }
        
        switch (contentType) {
            case "application/pdf":
                return "pdf";
            case "video/mp4":
                return "mp4";
            case "video/webm":
                return "webm";
            case "video/quicktime":
                return "mov";
            default:
                // Extract extension from content type (e.g., "video/mp4" -> "mp4")
                if (contentType.contains("/")) {
                    String subtype = contentType.split("/")[1];
                    if (subtype.contains(";")) {
                        subtype = subtype.split(";")[0];
                    }
                    return subtype;
                }
                return "bin";
        }
    }
    
    /**
     * Get CloudFront URL for a given S3 object key
     * @param objectKey The S3 object key
     * @return The CloudFront URL for the object
     */
    public String getCloudFrontUrl(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }
        
        // Format: https://[cloudfront-domain]/[object-key]
        return "https://" + cloudfrontDomain + "/" + objectKey;
    }
    
    /**
     * Upload course thumbnail image to S3 bucket
     * @param file The thumbnail image file to upload
     * @param courseId The course ID to associate with the thumbnail
     * @return The generated S3 object key for the uploaded thumbnail
     */
    public String uploadCourseThumbnail(MultipartFile file, Long courseId) {
        try {
            String contentType = file.getContentType();
            
            // Validate content type (ensure it's an image)
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid file type. Only images are allowed for course thumbnails.");
            }
            
            String extension = getExtensionFromContentType(contentType);
            
            // Generate a unique key for the thumbnail in S3 (in a course-thumbnails folder)
            String objectKey = "course-thumbnails/" + courseId + "-" + UUID.randomUUID() + "." + extension;
            
            // Create metadata for the file
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", contentType);
            metadata.put("course-id", courseId.toString());
            
            // Upload the thumbnail to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            return objectKey;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to upload thumbnail: " + e.getMessage());
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "S3 error: " + e.getMessage());
        }
    }
}