package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edunex.courseservice.model.enums.ModuleType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDTO {
    private Long id;
    private String userId;
    private Long moduleId;
    private String moduleTitle;
    private ModuleType moduleType;
    private boolean completed;
    private LocalDateTime completedAt;
    private Long courseId;
    private String courseTitle;
}
