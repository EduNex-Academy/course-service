package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserId(String userId);
    List<QuizResult> findByQuizId(Long quizId);
    Optional<QuizResult> findByUserIdAndQuizId(String userId, Long quizId);
    List<QuizResult> findByUserIdAndQuizIdOrderBySubmittedAtDesc(String userId, Long quizId);
    Optional<QuizResult> findTopByUserIdAndQuizIdOrderByScoreDesc(String userId, Long quizId);
}
