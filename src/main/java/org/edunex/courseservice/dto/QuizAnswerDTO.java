package org.edunex.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerDTO {
    private Long id;
    private String answerText;
    private boolean isCorrect;
    private Long questionId;
}
