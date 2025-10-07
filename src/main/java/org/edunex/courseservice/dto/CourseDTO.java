package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edunex.courseservice.model.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String instructorId;
    private String instructorName; // Optional - for display purposes
    private String category;
    private LocalDateTime createdAt;
    private String thumbnailUrl;   // URL to the course thumbnail image
    private CourseStatus status;   // Status of the course (DRAFT or PUBLISHED)

    // Statistics
    private int moduleCount;
    private int enrollmentCount;
    private double completionPercentage; // For the current user if specified

    // Related data
    private List<ModuleDTO> modules;
    private boolean userEnrolled;
}
