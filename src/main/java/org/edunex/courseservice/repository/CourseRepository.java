package org.edunex.courseservice.repository;

import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(String instructorId);
    List<Course> findByCategory(String category);
    List<Course> findByCategoryAndStatus(String category, CourseStatus status);
    List<Course> findByStatus(CourseStatus status);
    List<Course> findByInstructorIdAndStatus(String instructorId, CourseStatus status);

    @Query("SELECT DISTINCT c FROM Course c JOIN c.enrollments e WHERE e.userId = :userId")
    List<Course> findEnrolledCoursesByUserId(@Param("userId") String userId);

    @Query("SELECT DISTINCT c FROM Course c JOIN c.enrollments e WHERE e.userId = :userId AND c.status = :status")
    List<Course> findEnrolledCoursesByUserIdAndStatus(@Param("userId") String userId, @Param("status") CourseStatus status);

    // Modified query for case insensitive search
    @Query("SELECT c FROM Course c WHERE (UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))) AND c.status = :status")
    List<Course> searchCoursesByStatus(@Param("searchTerm") String searchTerm, @Param("status") CourseStatus status);
    
    @Query("SELECT c FROM Course c WHERE UPPER(c.title) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR UPPER(c.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);
}
