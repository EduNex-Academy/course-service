package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.EnrollmentDTO;
import org.edunex.courseservice.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByUserId(@PathVariable String userId) {
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
            @RequestParam String userId,
            @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentService.checkEnrollment(userId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
        return new ResponseEntity<>(createdEnrollment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<Void> unenrollUserFromCourse(
            @PathVariable String userId,
            @PathVariable Long courseId) {
        enrollmentService.unenrollUserFromCourse(userId, courseId);
        return ResponseEntity.noContent().build();
    }
}
