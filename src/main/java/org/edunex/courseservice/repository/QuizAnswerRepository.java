package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByQuestionId(Long questionId);
    List<QuizAnswer> findByQuestionIdAndCorrect(Long questionId, boolean correct);
}
