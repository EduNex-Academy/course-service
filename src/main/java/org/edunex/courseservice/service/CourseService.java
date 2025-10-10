package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.CourseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    List<CourseDTO> getAllCourses(String userId);
    List<CourseDTO> getAllCourses(String userId, CourseStatus status);
    CourseDTO getCourseById(Long id, String userId, boolean includeModules);
    CourseDTO getCourseById(Long id, String userId, boolean includeModules, CourseStatus requiredStatus);
    List<CourseDTO> getCoursesByInstructorId(String instructorId, String userId);
    List<CourseDTO> getCoursesByInstructorId(String instructorId, String userId, CourseStatus status);
    List<CourseDTO> getCoursesByCategory(String category, String userId);
    List<CourseDTO> getCoursesByCategory(String category, String userId, CourseStatus status);
    List<CourseDTO> getEnrolledCourses(String userId);
    List<CourseDTO> getEnrolledCourses(String userId, CourseStatus status);
    List<CourseDTO> searchCourses(String query, String userId);
    List<CourseDTO> searchCourses(String query, String userId, CourseStatus status);
    CourseDTO createCourse(CourseDTO courseDTO);
    CourseDTO updateCourse(Long id, CourseDTO courseDTO);
    void deleteCourse(Long id);
    
    /**
     * Upload a thumbnail image for a course
     * @param id The course ID
     * @param file The thumbnail image file
     * @return The updated course DTO with thumbnail URL
     */
    CourseDTO uploadCourseThumbnail(Long id, MultipartFile file);
    
    /**
     * Publish a course, changing its status from DRAFT to PUBLISHED
     * @param id The course ID
     * @param userId The ID of the user trying to publish the course
     * @return The updated course DTO with PUBLISHED status
     */
    CourseDTO publishCourse(Long id, String userId);

    // Helper methods for internal use
    CourseDTO mapToCourseDTO(Course course, String userId, boolean includeModules);
    List<CourseDTO> mapToCourseDTOs(List<Course> courses, String userId);
}
