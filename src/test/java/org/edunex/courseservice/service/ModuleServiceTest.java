package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.enums.ModuleType;
import org.edunex.courseservice.repository.CourseRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ProgressRepository progressRepository;
    
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ModuleService moduleService;

    private Module testModule;
    private Course testCourse;
    private Progress testProgress;
    private final String TEST_USER_ID = "user-123";
    private final Long TEST_COURSE_ID = 1L;
    private final Long TEST_MODULE_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test course
        testCourse = new Course();
        testCourse.setId(TEST_COURSE_ID);
        testCourse.setTitle("Test Course");
        
        // Setup test module
        testModule = new Module();
        testModule.setId(TEST_MODULE_ID);
        testModule.setTitle("Test Module");
        testModule.setType(ModuleType.VIDEO);
        testModule.setCoinsRequired(10);
        testModule.setContentUrl("https://example.com/video");
        testModule.setModuleOrder(1);
        testModule.setCourse(testCourse);
        
        // Setup test quiz for module
        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Test Quiz");
        quiz.setModule(testModule);
        testModule.setQuiz(quiz);
        
        // Setup test progress
        testProgress = new Progress();
        testProgress.setId(1L);
        testProgress.setUserId(TEST_USER_ID);
        testProgress.setModule(testModule);
        testProgress.setCompleted(true);
        testProgress.setCompletedAt(LocalDateTime.now());
        
        // Setup S3Service mock with lenient stubbing to avoid unnecessary stubbing errors
        lenient().when(s3Service.getCloudFrontUrl(anyString())).thenReturn("https://cloudfront.example.com/video");
    }

    @Test
    @DisplayName("Should return all modules")
    void getAllModules_shouldReturnAllModules() {
        // Arrange
        List<Module> modules = Arrays.asList(testModule);
        when(moduleRepository.findAll()).thenReturn(modules);

        // Act
        List<ModuleDTO> result = moduleService.getAllModules();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Module");
        verify(moduleRepository).findAll();
    }

    @Test
    @DisplayName("Should return module by id")
    void getModuleById_shouldReturnModule() {
        // Arrange
        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));

        // Act
        ModuleDTO result = moduleService.getModuleById(TEST_MODULE_ID, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_MODULE_ID);
        assertThat(result.getTitle()).isEqualTo("Test Module");
        verify(moduleRepository).findById(TEST_MODULE_ID);
    }

    @Test
    @DisplayName("Should return module by id with user progress")
    void getModuleById_withUserId_shouldReturnModuleWithProgress() {
        // Arrange
        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID))
            .thenReturn(Optional.of(testProgress));

        // Act
        ModuleDTO result = moduleService.getModuleById(TEST_MODULE_ID, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_MODULE_ID);
        assertThat(result.isCompleted()).isTrue();
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);
    }

    @Test
    @DisplayName("Should throw exception when module not found")
    void getModuleById_whenModuleNotFound_shouldThrowException() {
        // Arrange
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> moduleService.getModuleById(999L, null));
        verify(moduleRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return modules by course id")
    void getModulesByCourseId_shouldReturnModules() {
        // Arrange
        List<Module> modules = Arrays.asList(testModule);
        when(moduleRepository.findByCourseIdOrderByModuleOrder(TEST_COURSE_ID)).thenReturn(modules);

        // Act
        List<ModuleDTO> result = moduleService.getModulesByCourseId(TEST_COURSE_ID, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCourseId()).isEqualTo(TEST_COURSE_ID);
        verify(moduleRepository).findByCourseIdOrderByModuleOrder(TEST_COURSE_ID);
    }

    @Test
    @DisplayName("Should return modules by type")
    void getModulesByType_shouldReturnModules() {
        // Arrange
        List<Module> modules = Arrays.asList(testModule);
        when(moduleRepository.findByType(ModuleType.VIDEO)).thenReturn(modules);

        // Act
        List<ModuleDTO> result = moduleService.getModulesByType(ModuleType.VIDEO, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getType()).isEqualTo(ModuleType.VIDEO);
        verify(moduleRepository).findByType(ModuleType.VIDEO);
    }

    @Test
    @DisplayName("Should return available modules based on coins")
    void getAvailableModulesByCourseIdAndCoins_shouldReturnAvailableModules() {
        // Arrange
        int userCoins = 15;
        List<Module> modules = Arrays.asList(testModule);
        when(moduleRepository.findByCourseIdAndCoinsRequiredLessThanEqual(TEST_COURSE_ID, userCoins))
            .thenReturn(modules);

        // Act
        List<ModuleDTO> result = moduleService.getAvailableModulesByCourseIdAndCoins(
            TEST_COURSE_ID, userCoins, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCoinsRequired()).isLessThanOrEqualTo(userCoins);
        verify(moduleRepository).findByCourseIdAndCoinsRequiredLessThanEqual(TEST_COURSE_ID, userCoins);
    }

    @Test
    @DisplayName("Should create a new module")
    void createModule_shouldReturnCreatedModule() {
        // Arrange
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setTitle("New Module");
        moduleDTO.setType(ModuleType.VIDEO);
        moduleDTO.setCoinsRequired(5);
        moduleDTO.setContentUrl("https://example.com/new-video");
        moduleDTO.setModuleOrder(2);
        moduleDTO.setCourseId(TEST_COURSE_ID);

        Module newModule = new Module();
        newModule.setId(2L);
        newModule.setTitle(moduleDTO.getTitle());
        newModule.setType(moduleDTO.getType());
        newModule.setCoinsRequired(moduleDTO.getCoinsRequired());
        newModule.setContentUrl(moduleDTO.getContentUrl());
        newModule.setModuleOrder(moduleDTO.getModuleOrder());
        newModule.setCourse(testCourse);

        when(courseRepository.findById(TEST_COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(moduleRepository.save(any(Module.class))).thenReturn(newModule);

        // Act
        ModuleDTO result = moduleService.createModule(moduleDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Module");
        verify(courseRepository).findById(TEST_COURSE_ID);
        verify(moduleRepository).save(any(Module.class));
    }

    @Test
    @DisplayName("Should throw exception when creating module for non-existent course")
    void createModule_whenCourseNotFound_shouldThrowException() {
        // Arrange
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setTitle("New Module");
        moduleDTO.setCourseId(999L);

        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> moduleService.createModule(moduleDTO));
        verify(courseRepository).findById(999L);
        verify(moduleRepository, never()).save(any(Module.class));
    }

    @Test
    @DisplayName("Should update an existing module")
    void updateModule_shouldReturnUpdatedModule() {
        // Arrange
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setTitle("Updated Module");
        moduleDTO.setType(ModuleType.PDF);
        moduleDTO.setCoinsRequired(20);
        moduleDTO.setContentUrl("https://example.com/updated-content");
        moduleDTO.setModuleOrder(3);
        moduleDTO.setCourseId(TEST_COURSE_ID);

        Module existingModule = testModule;
        Module updatedModule = new Module();
        updatedModule.setId(TEST_MODULE_ID);
        updatedModule.setTitle(moduleDTO.getTitle());
        updatedModule.setType(moduleDTO.getType());
        updatedModule.setCoinsRequired(moduleDTO.getCoinsRequired());
        updatedModule.setContentUrl(moduleDTO.getContentUrl());
        updatedModule.setModuleOrder(moduleDTO.getModuleOrder());
        updatedModule.setCourse(testCourse);
        updatedModule.setQuiz(testModule.getQuiz());

        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(existingModule));
        when(courseRepository.findById(TEST_COURSE_ID)).thenReturn(Optional.of(testCourse));
        when(moduleRepository.save(any(Module.class))).thenReturn(updatedModule);

        // Act
        ModuleDTO result = moduleService.updateModule(TEST_MODULE_ID, moduleDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_MODULE_ID);
        assertThat(result.getTitle()).isEqualTo("Updated Module");
        assertThat(result.getType()).isEqualTo(ModuleType.PDF);
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(courseRepository).findById(TEST_COURSE_ID);
        verify(moduleRepository).save(any(Module.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent module")
    void updateModule_whenModuleNotFound_shouldThrowException() {
        // Arrange
        ModuleDTO moduleDTO = new ModuleDTO();
        moduleDTO.setTitle("Updated Module");

        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> moduleService.updateModule(999L, moduleDTO));
        verify(moduleRepository).findById(999L);
        verify(moduleRepository, never()).save(any(Module.class));
    }

    @Test
    @DisplayName("Should delete module")
    void deleteModule_shouldDeleteModule() {
        // Arrange
        when(moduleRepository.existsById(TEST_MODULE_ID)).thenReturn(true);
        doNothing().when(moduleRepository).deleteById(TEST_MODULE_ID);

        // Act
        moduleService.deleteModule(TEST_MODULE_ID);

        // Assert
        verify(moduleRepository).existsById(TEST_MODULE_ID);
        verify(moduleRepository).deleteById(TEST_MODULE_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent module")
    void deleteModule_whenModuleNotFound_shouldThrowException() {
        // Arrange
        when(moduleRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> moduleService.deleteModule(999L));
        verify(moduleRepository).existsById(999L);
        verify(moduleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should reorder a module")
    void reorderModule_shouldUpdateModuleOrder() {
        // Arrange
        int newOrder = 5;
        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);

        // Act
        moduleService.reorderModule(TEST_MODULE_ID, newOrder);

        // Assert
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(moduleRepository).save(any(Module.class));
    }

    @Test
    @DisplayName("Should throw exception when reordering non-existent module")
    void reorderModule_whenModuleNotFound_shouldThrowException() {
        // Arrange
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> moduleService.reorderModule(999L, 5));
        verify(moduleRepository).findById(999L);
        verify(moduleRepository, never()).save(any(Module.class));
    }
}
