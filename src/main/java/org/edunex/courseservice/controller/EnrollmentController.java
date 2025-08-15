package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.EnrollmentDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Enrollment;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @GetMapping
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        List<EnrollmentDTO> enrollmentDTOs = mapToEnrollmentDTOs(enrollments);
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));

        return ResponseEntity.ok(mapToEnrollmentDTO(enrollment));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByUserId(@PathVariable String userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        List<EnrollmentDTO> enrollmentDTOs = mapToEnrollmentDTOs(enrollments);
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByCourseId(@PathVariable Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<EnrollmentDTO> enrollmentDTOs = mapToEnrollmentDTOs(enrollments);
        return ResponseEntity.ok(enrollmentDTOs);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkEnrollment(
            @RequestParam String userId,
            @RequestParam Long courseId) {
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        return ResponseEntity.ok(isEnrolled);
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        // Check if the user is already enrolled in the course
        if (enrollmentRepository.existsByUserIdAndCourseId(enrollmentDTO.getUserId(), enrollmentDTO.getCourseId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already enrolled in this course");
        }

        Course course = courseRepository.findById(enrollmentDTO.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(enrollmentDTO.getUserId());
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return new ResponseEntity<>(mapToEnrollmentDTO(savedEnrollment), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found");
        }

        enrollmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<Void> unenrollUserFromCourse(
            @PathVariable String userId,
            @PathVariable Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User is not enrolled in this course"));

        enrollmentRepository.delete(enrollment);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
    private EnrollmentDTO mapToEnrollmentDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUserId());
        dto.setEnrolledAt(enrollment.getEnrolledAt());

        if (enrollment.getCourse() != null) {
            dto.setCourseId(enrollment.getCourse().getId());
            dto.setCourseTitle(enrollment.getCourse().getTitle());

            // Calculate completion percentage
            Long completedModules = progressRepository.countCompletedModulesByCourseAndUser(
                    enrollment.getUserId(), enrollment.getCourse().getId());

            Long totalModules = progressRepository.countModulesByCourse(enrollment.getCourse().getId());

            double completionPercentage = totalModules > 0 ?
                    (double) completedModules / totalModules * 100 : 0;

            dto.setCompletionPercentage(completionPercentage);
        }

        return dto;
    }

    private List<EnrollmentDTO> mapToEnrollmentDTOs(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(this::mapToEnrollmentDTO)
                .collect(Collectors.toList());
    }
}
