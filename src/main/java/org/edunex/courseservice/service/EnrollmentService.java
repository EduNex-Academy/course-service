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

    public EnrollmentDTO createEnrollment(EnrollmentDTO enrollmentDTO) {
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
        return mapToEnrollmentDTO(savedEnrollment);
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
