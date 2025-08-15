package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.edunex.courseservice.model.enums.ModuleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    private Long id;
    private String title;
    private ModuleType type;
    private int coinsRequired;
    private String contentUrl;
    private int moduleOrder;
    private Long courseId;
    private String courseName;
    private Long quizId;
    private boolean completed;
    private double progressPercentage;
}
