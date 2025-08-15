package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByModuleId(Long moduleId);
    Optional<Quiz> findByTitle(String title);
}
