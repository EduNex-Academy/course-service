package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.EnrollmentDTO;
import org.edunex.courseservice.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDTO enrollmentDTO = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollmentDTO);
    }

    @GetMapping("/user")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByUserId(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getEnrollmentsByUserId(userId);
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        List<EnrollmentDTO> enrollmentDTOs = enrollmentService.getEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkEnrollment(
            @RequestParam Long courseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        boolean isEnrolled = enrollmentService.checkEnrollment(userId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    /**
     * Enroll the current user in a course (original method)
     * @deprecated Use {@link #enrollInCourse(Long, Jwt)} instead
     */
    @Deprecated
    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }
    
    /**
     * Enroll the current user in a course
     * 
     * @param courseId The ID of the course to enroll in
     * @param jwt The JWT token containing user information
     * @return The created enrollment details
     */
    @PostMapping("/course/{courseId}")
    public ResponseEntity<EnrollmentDTO> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(userId, courseId);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<Void> unenrollUserFromCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        enrollmentService.unenrollUserFromCourse(userId, courseId);
        return ResponseEntity.noContent().build();
    }
}
