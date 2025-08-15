package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDTO {
    private Long id;
    private String userId;
    private Long quizId;
    private String quizTitle;
    private int score;
    private LocalDateTime submittedAt;

    // Additional fields for displaying more info in the UI
    private String moduleTitle;
    private Long courseId;
    private String courseTitle;
}
