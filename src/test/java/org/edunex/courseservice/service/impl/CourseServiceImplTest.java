package org.edunex.courseservice.service.impl;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.enums.ModuleType;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ModuleRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ProgressRepository progressRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course testCourse;
    private Module testModule;
    private List<Course> testCourses;
    private List<Module> testModules;
    private final String TEST_USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        // Setup test course
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setTitle("Test Course");
        testCourse.setDescription("This is a test course");
        testCourse.setInstructorId("instructor-456");
        testCourse.setCategory("Programming");
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse.setModules(new ArrayList<>());
        testCourse.setEnrollments(new ArrayList<>());

        // Setup test module
        testModule = new Module();
        testModule.setId(1L);
        testModule.setTitle("Test Module");
        testModule.setType(ModuleType.VIDEO);
        testModule.setCoinsRequired(10);
        testModule.setContentUrl("https://example.com/video");
        testModule.setModuleOrder(1);
        testModule.setCourse(testCourse);

        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Test Quiz");
        quiz.setModule(testModule);
        testModule.setQuiz(quiz);

        testCourse.getModules().add(testModule);

        // Setup test course list
        testCourses = Arrays.asList(testCourse);
        
        // Setup test module list
        testModules = Arrays.asList(testModule);
    }

    @Test
    @DisplayName("Should return all courses when userId is null")
    void getAllCourses_withNullUserId_shouldReturnAllCourses() {
        // Arrange
        when(courseRepository.findAll()).thenReturn(testCourses);

        // Act
        List<CourseDTO> result = courseService.getAllCourses(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Course");
        verify(courseRepository).findAll();
        verify(courseRepository, never()).findEnrolledCoursesByUserId(any());
    }

    @Test
    @DisplayName("Should return enrolled courses when userId is provided")
    void getAllCourses_withUserId_shouldReturnEnrolledCourses() {
        // Arrange
        when(courseRepository.findEnrolledCoursesByUserId(TEST_USER_ID)).thenReturn(testCourses);

        // Act
        List<CourseDTO> result = courseService.getAllCourses(TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Course");
        verify(courseRepository).findEnrolledCoursesByUserId(TEST_USER_ID);
        verify(courseRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should get course by id without including modules")
    void getCourseById_withoutModules_shouldReturnCourse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act
        CourseDTO result = courseService.getCourseById(1L, null, false);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Course");
        assertThat(result.getModules()).isNull();
        verify(courseRepository).findById(1L);
        verify(moduleRepository, never()).findByCourseIdOrderByModuleOrder(anyLong());
    }

    @Test
    @DisplayName("Should get course by id including modules")
    void getCourseById_withModules_shouldReturnCourseWithModules() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(moduleRepository.findByCourseIdOrderByModuleOrder(1L)).thenReturn(testModules);

        // Act
        CourseDTO result = courseService.getCourseById(1L, null, true);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Course");
        assertThat(result.getModules()).isNotNull();
        assertThat(result.getModules().size()).isEqualTo(1);
        assertThat(result.getModules().get(0).getTitle()).isEqualTo("Test Module");
        verify(courseRepository).findById(1L);
        verify(moduleRepository).findByCourseIdOrderByModuleOrder(1L);
    }

    @Test
    @DisplayName("Should throw exception when course not found")
    void getCourseById_whenCourseNotFound_shouldThrowException() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> courseService.getCourseById(999L, null, false)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(404);
        assertThat(exception.getReason()).isEqualTo("Course not found");
        verify(courseRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get courses by instructor id")
    void getCoursesByInstructorId_shouldReturnCourses() {
        // Arrange
        String instructorId = "instructor-456";
        when(courseRepository.findByInstructorId(instructorId)).thenReturn(testCourses);

        // Act
        List<CourseDTO> result = courseService.getCoursesByInstructorId(instructorId, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getInstructorId()).isEqualTo(instructorId);
        verify(courseRepository).findByInstructorId(instructorId);
    }

    @Test
    @DisplayName("Should get courses by category")
    void getCoursesByCategory_shouldReturnCourses() {
        // Arrange
        String category = "Programming";
        when(courseRepository.findByCategory(category)).thenReturn(testCourses);

        // Act
        List<CourseDTO> result = courseService.getCoursesByCategory(category, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCategory()).isEqualTo(category);
        verify(courseRepository).findByCategory(category);
    }

    @Test
    @DisplayName("Should get enrolled courses for a user")
    void getEnrolledCourses_shouldReturnCourses() {
        // Arrange
        when(courseRepository.findEnrolledCoursesByUserId(TEST_USER_ID)).thenReturn(testCourses);

        // Act
        List<CourseDTO> result = courseService.getEnrolledCourses(TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        verify(courseRepository).findEnrolledCoursesByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should search courses by query")
    void searchCourses_shouldReturnMatchingCourses() {
        // Arrange
        String query = "test";
        when(courseRepository.searchCourses(query)).thenReturn(testCourses);

        // Act
        List<CourseDTO> result = courseService.searchCourses(query, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        verify(courseRepository).searchCourses(query);
    }

    @Test
    @DisplayName("Should create a new course")
    void createCourse_shouldReturnCreatedCourse() {
        // Arrange
        CourseDTO inputCourseDTO = new CourseDTO();
        inputCourseDTO.setTitle("New Course");
        inputCourseDTO.setDescription("New course description");
        inputCourseDTO.setInstructorId("instructor-456");
        inputCourseDTO.setCategory("Programming");

        Course savedCourse = new Course();
        savedCourse.setId(2L);
        savedCourse.setTitle(inputCourseDTO.getTitle());
        savedCourse.setDescription(inputCourseDTO.getDescription());
        savedCourse.setInstructorId(inputCourseDTO.getInstructorId());
        savedCourse.setCategory(inputCourseDTO.getCategory());
        savedCourse.setCreatedAt(LocalDateTime.now());
        savedCourse.setModules(new ArrayList<>());
        savedCourse.setEnrollments(new ArrayList<>());

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        // Act
        CourseDTO result = courseService.createCourse(inputCourseDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Course");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should update an existing course")
    void updateCourse_shouldReturnUpdatedCourse() {
        // Arrange
        Long courseId = 1L;
        CourseDTO updateCourseDTO = new CourseDTO();
        updateCourseDTO.setTitle("Updated Course Title");
        updateCourseDTO.setDescription("Updated description");
        updateCourseDTO.setCategory("Updated Category");

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Old Course Title");
        existingCourse.setDescription("Old description");
        existingCourse.setInstructorId("instructor-456");
        existingCourse.setCategory("Old Category");
        existingCourse.setCreatedAt(LocalDateTime.now());

        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setTitle(updateCourseDTO.getTitle());
        updatedCourse.setDescription(updateCourseDTO.getDescription());
        updatedCourse.setInstructorId("instructor-456"); // Unchanged
        updatedCourse.setCategory(updateCourseDTO.getCategory());
        updatedCourse.setCreatedAt(existingCourse.getCreatedAt());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);

        // Act
        CourseDTO result = courseService.updateCourse(courseId, updateCourseDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courseId);
        assertThat(result.getTitle()).isEqualTo("Updated Course Title");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getCategory()).isEqualTo("Updated Category");
        assertThat(result.getInstructorId()).isEqualTo("instructor-456"); // Should remain unchanged
        verify(courseRepository).findById(courseId);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent course")
    void updateCourse_whenCourseNotFound_shouldThrowException() {
        // Arrange
        Long courseId = 999L;
        CourseDTO updateCourseDTO = new CourseDTO();
        updateCourseDTO.setTitle("Updated Course Title");

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> courseService.updateCourse(courseId, updateCourseDTO)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(404);
        assertThat(exception.getReason()).isEqualTo("Course not found");
        verify(courseRepository).findById(courseId);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should delete a course")
    void deleteCourse_shouldDeleteCourse() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.existsById(courseId)).thenReturn(true);
        doNothing().when(courseRepository).deleteById(courseId);

        // Act
        courseService.deleteCourse(courseId);

        // Assert
        verify(courseRepository).existsById(courseId);
        verify(courseRepository).deleteById(courseId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent course")
    void deleteCourse_whenCourseNotFound_shouldThrowException() {
        // Arrange
        Long courseId = 999L;
        when(courseRepository.existsById(courseId)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> courseService.deleteCourse(courseId)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(404);
        assertThat(exception.getReason()).isEqualTo("Course not found");
        verify(courseRepository).existsById(courseId);
        verify(courseRepository, never()).deleteById(courseId);
    }

    @Test
    @DisplayName("Should map course to DTO with enrollment status and progress")
    void mapToCourseDTO_withUserEnrolled_shouldIncludeEnrollmentAndProgress() {
        // Arrange
        when(enrollmentRepository.existsByUserIdAndCourseId(TEST_USER_ID, 1L)).thenReturn(true);
        when(progressRepository.countCompletedModulesByCourseAndUser(TEST_USER_ID, 1L)).thenReturn(1L);

        // Act
        CourseDTO result = courseService.mapToCourseDTO(testCourse, TEST_USER_ID, false);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isUserEnrolled()).isTrue();
        assertThat(result.getCompletionPercentage()).isEqualTo(100.0); // 1 completed out of 1 total
        verify(enrollmentRepository).existsByUserIdAndCourseId(TEST_USER_ID, 1L);
        verify(progressRepository).countCompletedModulesByCourseAndUser(TEST_USER_ID, 1L);
    }

    @Test
    @DisplayName("Should map course list to DTO list")
    void mapToCourseDTOs_shouldMapAllCourses() {
        // Arrange
        List<Course> courses = Arrays.asList(testCourse, testCourse); // Using same course twice for simplicity

        // Act
        List<CourseDTO> result = courseService.mapToCourseDTOs(courses, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Course");
        assertThat(result.get(1).getTitle()).isEqualTo("Test Course");
    }
}
