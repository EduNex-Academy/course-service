package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.CourseDTO;
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
            @AuthenticationPrincipal Jwt jwt) {
        String userId = filterByUser ? jwt.getSubject() : null;
        List<CourseDTO> courseDTOs = courseService.getAllCourses(userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeModules,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        CourseDTO courseDTO = courseService.getCourseById(id, userId, includeModules);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructorId(
            @PathVariable String instructorId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<CourseDTO> courseDTOs = courseService.getCoursesByInstructorId(instructorId, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<CourseDTO> courseDTOs = courseService.getCoursesByCategory(category, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDTO>> getEnrolledCourses(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<CourseDTO> courseDTOs = courseService.getEnrolledCourses(userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(
            @RequestParam String query,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<CourseDTO> courseDTOs = courseService.searchCourses(query, userId);
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
}
