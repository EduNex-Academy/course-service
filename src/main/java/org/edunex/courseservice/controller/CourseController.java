package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.service.impl.CourseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseServiceImpl courseService;

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses(@RequestParam(required = false) String userId) {
        List<CourseDTO> courseDTOs = courseService.getAllCourses(userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(
            @PathVariable Long id,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "false") boolean includeModules) {
        CourseDTO courseDTO = courseService.getCourseById(id, userId, includeModules);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructorId(
            @PathVariable String instructorId,
            @RequestParam(required = false) String userId) {
        List<CourseDTO> courseDTOs = courseService.getCoursesByInstructorId(instructorId, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String userId) {
        List<CourseDTO> courseDTOs = courseService.getCoursesByCategory(category, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDTO>> getEnrolledCourses(@RequestParam String userId) {
        List<CourseDTO> courseDTOs = courseService.getEnrolledCourses(userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(
            @RequestParam String query,
            @RequestParam(required = false) String userId) {
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
}
