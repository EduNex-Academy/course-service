package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.model.Course;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    List<CourseDTO> getAllCourses(String userId);
    CourseDTO getCourseById(Long id, String userId, boolean includeModules);
    List<CourseDTO> getCoursesByInstructorId(String instructorId, String userId);
    List<CourseDTO> getCoursesByCategory(String category, String userId);
    List<CourseDTO> getEnrolledCourses(String userId);
    List<CourseDTO> searchCourses(String query, String userId);
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

    // Helper methods for internal use
    CourseDTO mapToCourseDTO(Course course, String userId, boolean includeModules);
    List<CourseDTO> mapToCourseDTOs(List<Course> courses, String userId);
}
