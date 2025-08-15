package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(String instructorId);
    List<Course> findByCategory(String category);

    @Query("SELECT DISTINCT c FROM Course c JOIN c.enrollments e WHERE e.userId = :userId")
    List<Course> findEnrolledCoursesByUserId(@Param("userId") String userId);

    // Modified query to avoid using LOWER on CLOB field
    @Query("SELECT c FROM Course c WHERE c.title LIKE CONCAT('%', :searchTerm, '%') OR c.description LIKE CONCAT('%', :searchTerm, '%')")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);
}
