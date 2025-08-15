package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserId(String userId);
    List<Enrollment> findByCourseId(Long courseId);
    Optional<Enrollment> findByUserIdAndCourseId(String userId, Long courseId);
    boolean existsByUserIdAndCourseId(String userId, Long courseId);
}
