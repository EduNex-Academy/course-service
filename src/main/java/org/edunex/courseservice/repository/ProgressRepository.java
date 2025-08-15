package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByUserId(String userId);
    List<Progress> findByModuleId(Long moduleId);
    Optional<Progress> findByUserIdAndModuleId(String userId, Long moduleId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.userId = :userId AND p.module.course.id = :courseId AND p.completed = true")
    Long countCompletedModulesByCourseAndUser(@Param("userId") String userId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(m) FROM Module m WHERE m.course.id = :courseId")
    Long countModulesByCourse(@Param("courseId") Long courseId);

    @Query("SELECT p FROM Progress p WHERE p.userId = :userId AND p.module.course.id = :courseId")
    List<Progress> findByCourseIdAndUserId(@Param("userId") String userId, @Param("courseId") Long courseId);
}
