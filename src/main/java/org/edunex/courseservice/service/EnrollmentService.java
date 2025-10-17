package org.edunex.courseservice.service;

import lombok.RequiredArgsConstructor;
import org.edunex.courseservice.dto.EnrollmentDTO;
import org.edunex.courseservice.event.CourseEmailEvent;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Enrollment;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;
    private final CourseEventProducer courseEventProducer;

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
     * @param jwt The JWT token containing user claims (can be null)
     * @return The created enrollment details
     */
    public EnrollmentDTO createEnrollment(String userId, Long courseId, Jwt jwt) {
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

        // Send email event with data extracted from JWT
        String email;
        String studentName;
        
        if (jwt != null) {
            email = extractEmailFromJwt(jwt, userId);
            studentName = extractNameFromJwt(jwt, userId);
        } else {
            // Fallback if JWT not provided
            email = userId.contains("@") ? userId : userId + "@edunex.academy";
            studentName = userId.contains("@") ? userId.substring(0, userId.indexOf('@')).replace(".", " ") : userId;
        }
        
        CourseEmailEvent emailEvent = new CourseEmailEvent(
                email, // email extracted from JWT or userId
                course.getTitle(),
                studentName, // name extracted from JWT or userId
                "COURSE_ENROLLMENT"
        );
        courseEventProducer.sendEmailEvent(emailEvent);

        return mapToEnrollmentDTO(savedEnrollment);
    }
    
    /**
     * Create an enrollment for the specified user and course
     * 
     * @param userId The ID of the user enrolling in the course
     * @param courseId The ID of the course to enroll in
     * @return The created enrollment details
     */
    public EnrollmentDTO createEnrollment(String userId, Long courseId) {
        // Call the overloaded method with JWT as null
        return createEnrollment(userId, courseId, null);
    }
    
    /**
     * Create an enrollment from an EnrollmentDTO
     * @deprecated Use {@link #createEnrollment(String, Long)} instead
     * @param enrollmentDTO The enrollment data
     * @return The created enrollment details
     */
    @Deprecated
    public EnrollmentDTO createEnrollment(EnrollmentDTO enrollmentDTO) {
        return createEnrollment(enrollmentDTO.getUserId(), enrollmentDTO.getCourseId(), null);
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
    
    /**
     * Extract email from JWT token claims
     * 
     * @param jwt The JWT token
     * @param userId Fallback user ID if email claim is not present
     * @return Email from the JWT or constructed email from userId
     */
    private String extractEmailFromJwt(Jwt jwt, String userId) {
        // Try to get email claim from the JWT
        String email = jwt.getClaim("email");
        if (email != null && !email.isEmpty()) {
            return email;
        }
        
        // Check if userId is already an email format
        if (userId != null && userId.contains("@")) {
            return userId;
        }
        
        // If no email found, construct one from userId
        return userId + "@edunex.academy";
    }
    
    /**
     * Extract user's name from JWT token claims
     * 
     * @param jwt The JWT token
     * @param userId Fallback user ID if name claims are not present
     * @return User's name from JWT or extracted from userId
     */
    private String extractNameFromJwt(Jwt jwt, String userId) {
        // Try to get name from JWT claims in order of preference
        String name = jwt.getClaim("name");
        if (name == null || name.isEmpty()) {
            name = jwt.getClaim("given_name");
        }
        if (name == null || name.isEmpty()) {
            name = jwt.getClaim("preferred_username");
        }
        
        // If name found in JWT, return it
        if (name != null && !name.isEmpty()) {
            return name;
        }
        
        // Extract name from userId if it's an email
        if (userId != null && userId.contains("@")) {
            String localPart = userId.substring(0, userId.indexOf('@'));
            return localPart.replace(".", " ");
        }
        
        // Fallback to userId
        return userId;
    }
}
