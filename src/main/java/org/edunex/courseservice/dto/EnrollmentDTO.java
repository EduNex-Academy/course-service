package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    private Long id;
    private String userId;
    private String userName; // Optional - for display purposes
    private Long courseId;
    private String courseTitle;
    private LocalDateTime enrolledAt;
    private double completionPercentage; // Progress percentage in the course
}
