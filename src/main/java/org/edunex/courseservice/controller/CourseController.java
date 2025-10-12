package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.model.CourseStatus;
import org.edunex.courseservice.service.impl.CourseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseServiceImpl courseService;

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses(
            @RequestParam(required = false, defaultValue = "false") boolean filterByUser,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false, defaultValue = "false") boolean includeInstructorCourses,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = filterByUser || includeInstructorCourses ? jwt.getSubject() : null;
        
        // Default to PUBLISHED for non-instructor requests if status not specified
        CourseStatus effectiveStatus = status;
        if (!includeInstructorCourses && status == null) {
            effectiveStatus = CourseStatus.PUBLISHED;
        }
        
        List<CourseDTO> courseDTOs = courseService.getAllCourses(userId, effectiveStatus);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeModules,
            @RequestParam(required = false) CourseStatus status, // Optional status for instructors to view drafts
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        CourseDTO courseDTO = courseService.getCourseById(id, userId, includeModules, status);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructorId(
            @PathVariable String instructorId,
            @RequestParam(required = false) CourseStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        
        // Check if the user is viewing their own courses
        boolean isOwnCourses = userId != null && userId.equals(instructorId);
        
        // If instructor is viewing their own courses and no status is specified,
        // show all courses (both DRAFT and PUBLISHED)
        if (isOwnCourses && status == null) {
            // Pass null for status to get all courses
            List<CourseDTO> courseDTOs = courseService.getCoursesByInstructorId(instructorId, userId, null);
            return ResponseEntity.ok(courseDTOs);
        } else {
            // Default to PUBLISHED if status is null and not viewing own courses
            CourseStatus effectiveStatus = status != null ? status : CourseStatus.PUBLISHED;
            List<CourseDTO> courseDTOs = courseService.getCoursesByInstructorId(instructorId, userId, effectiveStatus);
            return ResponseEntity.ok(courseDTOs);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(
            @PathVariable String category,
            @RequestParam(required = false, defaultValue = "PUBLISHED") CourseStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<CourseDTO> courseDTOs = courseService.getCoursesByCategory(category, userId, status);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDTO>> getEnrolledCourses(
            @RequestParam(required = false, defaultValue = "PUBLISHED") CourseStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<CourseDTO> courseDTOs = courseService.getEnrolledCourses(userId, status);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "PUBLISHED") CourseStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<CourseDTO> courseDTOs = courseService.searchCourses(query, userId, status);
        return ResponseEntity.ok(courseDTOs);
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Upload a thumbnail image for a course
     * 
     * @param id The ID of the course
     * @param file The thumbnail image file
     * @return The updated course DTO with thumbnail URL
     */
    @PostMapping(value = "/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseDTO> uploadCourseThumbnail(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        CourseDTO updatedCourse = courseService.uploadCourseThumbnail(id, file);
        return ResponseEntity.ok(updatedCourse);
    }
    
    /**
     * Publish a course, changing its status from DRAFT to PUBLISHED
     * 
     * @param id The ID of the course to publish
     * @param jwt The JWT token containing user information
     * @return The updated course DTO with PUBLISHED status
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<CourseDTO> publishCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        CourseDTO publishedCourse = courseService.publishCourse(id, userId);
        return ResponseEntity.ok(publishedCourse);
    }
    
    /**
     * Get all courses created by the currently authenticated instructor.
     * This endpoint shows both DRAFT and PUBLISHED courses.
     * 
     * @param status Optional filter by status
     * @param jwt The JWT token containing user information
     * @return List of courses created by the instructor
     */
    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseDTO>> getMyInstructorCourses(
            @RequestParam(required = false) CourseStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        
        String instructorId = jwt.getSubject();
        List<CourseDTO> courseDTOs = courseService.getCoursesByInstructorId(instructorId, instructorId, status);
        return ResponseEntity.ok(courseDTOs);
    }
}
