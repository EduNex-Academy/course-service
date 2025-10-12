package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.EnrollmentDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Enrollment;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgressRepository progressRepository;

    public List<EnrollmentDTO> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        return mapToEnrollmentDTOs(enrollments);
    }

    public EnrollmentDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        return mapToEnrollmentDTO(enrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsByUserId(String userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
        return mapToEnrollmentDTOs(enrollments);
    }

    public List<EnrollmentDTO> getEnrollmentsByCourseId(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return mapToEnrollmentDTOs(enrollments);
    }

    public boolean checkEnrollment(String userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Create an enrollment for the specified user and course
     * 
     * @param userId The ID of the user enrolling in the course
     * @param courseId The ID of the course to enroll in
     * @return The created enrollment details
     */
    public EnrollmentDTO createEnrollment(String userId, Long courseId) {
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already enrolled in this course");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return mapToEnrollmentDTO(savedEnrollment);
    }
    
    /**
     * Create an enrollment from an EnrollmentDTO
     * @deprecated Use {@link #createEnrollment(String, Long)} instead
     * @param enrollmentDTO The enrollment data
     * @return The created enrollment details
     */
    @Deprecated
    public EnrollmentDTO createEnrollment(EnrollmentDTO enrollmentDTO) {
        return createEnrollment(enrollmentDTO.getUserId(), enrollmentDTO.getCourseId());
    }

    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found");
        }
        enrollmentRepository.deleteById(id);
    }

    public void unenrollUserFromCourse(String userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not enrolled in this course"));
        enrollmentRepository.delete(enrollment);
    }

    private EnrollmentDTO mapToEnrollmentDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setUserId(enrollment.getUserId());
        dto.setEnrolledAt(enrollment.getEnrolledAt());

        if (enrollment.getCourse() != null) {
            dto.setCourseId(enrollment.getCourse().getId());
            dto.setCourseTitle(enrollment.getCourse().getTitle());

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
