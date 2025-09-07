package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.EnrollmentDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Enrollment;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Enrollment testEnrollment;
    private Course testCourse;
    private final String TEST_USER_ID = "user-123";
    private final Long TEST_COURSE_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test course
        testCourse = new Course();
        testCourse.setId(TEST_COURSE_ID);
        testCourse.setTitle("Test Course");
        
        // Setup test enrollment
        testEnrollment = new Enrollment();
        testEnrollment.setId(1L);
        testEnrollment.setUserId(TEST_USER_ID);
        testEnrollment.setCourse(testCourse);
        testEnrollment.setEnrolledAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return all enrollments")
    void getAllEnrollments_shouldReturnAllEnrollments() {
        // Arrange
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findAll()).thenReturn(enrollments);

        // Act
        List<EnrollmentDTO> result = enrollmentService.getAllEnrollments();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        verify(enrollmentRepository).findAll();
    }

    @Test
    @DisplayName("Should return enrollment by id")
    void getEnrollmentById_shouldReturnEnrollment() {
        // Arrange
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));

        // Act
        EnrollmentDTO result = enrollmentService.getEnrollmentById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        verify(enrollmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when enrollment not found")
    void getEnrollmentById_whenEnrollmentNotFound_shouldThrowException() {
        // Arrange
        when(enrollmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> enrollmentService.getEnrollmentById(999L));
        verify(enrollmentRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return enrollments by user id")
    void getEnrollmentsByUserId_shouldReturnEnrollments() {
        // Arrange
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findByUserId(TEST_USER_ID)).thenReturn(enrollments);

        // Act
        List<EnrollmentDTO> result = enrollmentService.getEnrollmentsByUserId(TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        verify(enrollmentRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return enrollments by course id")
    void getEnrollmentsByCourseId_shouldReturnEnrollments() {
        // Arrange
        List<Enrollment> enrollments = Arrays.asList(testEnrollment);
        when(enrollmentRepository.findByCourseId(TEST_COURSE_ID)).thenReturn(enrollments);

        // Act
        List<EnrollmentDTO> result = enrollmentService.getEnrollmentsByCourseId(TEST_COURSE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCourseId()).isEqualTo(TEST_COURSE_ID);
        verify(enrollmentRepository).findByCourseId(TEST_COURSE_ID);
    }

    @Test
    @DisplayName("Should check if user is enrolled in a course")
    void checkEnrollment_shouldReturnEnrollmentStatus() {
        // Arrange
        when(enrollmentRepository.existsByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID)).thenReturn(true);

        // Act
        boolean result = enrollmentService.checkEnrollment(TEST_USER_ID, TEST_COURSE_ID);

        // Assert
        assertThat(result).isTrue();
        verify(enrollmentRepository).existsByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID);
    }

    @Test
    @DisplayName("Should create a new enrollment")
    void createEnrollment_shouldReturnCreatedEnrollment() {
        // Arrange
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
        enrollmentDTO.setUserId(TEST_USER_ID);
        enrollmentDTO.setCourseId(TEST_COURSE_ID);

        when(courseRepository.findById(TEST_COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        // Act
        EnrollmentDTO result = enrollmentService.createEnrollment(enrollmentDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getCourseId()).isEqualTo(TEST_COURSE_ID);
        verify(courseRepository).findById(TEST_COURSE_ID);
        verify(enrollmentRepository).existsByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw exception when creating enrollment for non-existent course")
    void createEnrollment_whenCourseNotFound_shouldThrowException() {
        // Arrange
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
        enrollmentDTO.setUserId(TEST_USER_ID);
        enrollmentDTO.setCourseId(999L);

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> enrollmentService.createEnrollment(enrollmentDTO));
        verify(courseRepository).findById(999L);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw exception when user is already enrolled")
    void createEnrollment_whenUserAlreadyEnrolled_shouldThrowException() {
        // Arrange
        EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
        enrollmentDTO.setUserId(TEST_USER_ID);
        enrollmentDTO.setCourseId(TEST_COURSE_ID);

        when(courseRepository.findById(TEST_COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID)).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> enrollmentService.createEnrollment(enrollmentDTO));
        verify(courseRepository).findById(TEST_COURSE_ID);
        verify(enrollmentRepository).existsByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID);
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should delete enrollment")
    void deleteEnrollment_shouldDeleteEnrollment() {
        // Arrange
        when(enrollmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(enrollmentRepository).deleteById(1L);

        // Act
        enrollmentService.deleteEnrollment(1L);

        // Assert
        verify(enrollmentRepository).existsById(1L);
        verify(enrollmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent enrollment")
    void deleteEnrollment_whenEnrollmentNotFound_shouldThrowException() {
        // Arrange
        when(enrollmentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> enrollmentService.deleteEnrollment(999L));
        verify(enrollmentRepository).existsById(999L);
        verify(enrollmentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should unenroll user from course")
    void unenrollUserFromCourse_shouldDeleteEnrollment() {
        // Arrange
        Optional<Enrollment> enrollmentOptional = Optional.of(testEnrollment);
        when(enrollmentRepository.findByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID)).thenReturn(enrollmentOptional);
        doNothing().when(enrollmentRepository).deleteById(1L);

        // Act
        enrollmentService.unenrollUserFromCourse(TEST_USER_ID, TEST_COURSE_ID);

        // Assert
        verify(enrollmentRepository).findByUserIdAndCourseId(TEST_USER_ID, TEST_COURSE_ID);
        verify(enrollmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when unenrolling from non-existent enrollment")
    void unenrollUserFromCourse_whenEnrollmentNotFound_shouldThrowException() {
        // Arrange
        when(enrollmentRepository.findByUserIdAndCourseId(TEST_USER_ID, 999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> enrollmentService.unenrollUserFromCourse(TEST_USER_ID, 999L));
        verify(enrollmentRepository).findByUserIdAndCourseId(TEST_USER_ID, 999L);
        verify(enrollmentRepository, never()).deleteById(anyLong());
    }
}
