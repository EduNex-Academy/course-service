package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDTO {
    private Long id;
    private String questionText;
    private Long quizId;
    private List<QuizAnswerDTO> answers;
}
