package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private Long moduleId;
    private String fileName;
    private String contentType;
    private String objectKey;
    private String url;  // CloudFront URL
    private long size;
    private String fileType; // "VIDEO" or "PDF"
    
    // Constructor for upload response without CloudFront URL
    public FileDTO(Long moduleId, String fileName, String contentType, String objectKey, long size) {
        this.moduleId = moduleId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.objectKey = objectKey;
        this.size = size;
        
        // Determine file type based on content type
        if (contentType != null) {
            if (contentType.startsWith("video/")) {
                this.fileType = "VIDEO";
            } else if (contentType.equals("application/pdf")) {
                this.fileType = "PDF";
            } else {
                this.fileType = "OTHER";
            }
        }
    }
    
    // Constructor with CloudFront URL
    public FileDTO(Long moduleId, String fileName, String contentType, String objectKey, String cloudFrontUrl, long size) {
        this(moduleId, fileName, contentType, objectKey, size);
        this.url = cloudFrontUrl;
    }
}