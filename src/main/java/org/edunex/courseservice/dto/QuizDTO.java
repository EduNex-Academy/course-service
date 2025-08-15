package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDTO {
    private Long id;
    private String title;
    private Long moduleId;
    private String moduleTitle;
    private List<QuizQuestionDTO> questions;
}
