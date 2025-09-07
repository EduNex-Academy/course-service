package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.ProgressDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.model.enums.ModuleType;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private ProgressService progressService;

    private Progress testProgress;
    private Module testModule;
    private Course testCourse;
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
        testModule.setCourse(testCourse);
        
        // Setup test progress
        testProgress = new Progress();
        testProgress.setId(1L);
        testProgress.setUserId(TEST_USER_ID);
        testProgress.setModule(testModule);
        testProgress.setCompleted(true);
        testProgress.setCompletedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return all progress records")
    void getAllProgress_shouldReturnAllProgressRecords() {
        // Arrange
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findAll()).thenReturn(progressList);

        // Act
        List<ProgressDTO> result = progressService.getAllProgress();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        verify(progressRepository).findAll();
    }

    @Test
    @DisplayName("Should return progress by id")
    void getProgressById_shouldReturnProgress() {
        // Arrange
        when(progressRepository.findById(1L)).thenReturn(Optional.of(testProgress));

        // Act
        ProgressDTO result = progressService.getProgressById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.isCompleted()).isTrue();
        verify(progressRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when progress not found")
    void getProgressById_whenProgressNotFound_shouldThrowException() {
        // Arrange
        when(progressRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> progressService.getProgressById(999L));
        verify(progressRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return progress by user id")
    void getProgressByUserId_shouldReturnProgress() {
        // Arrange
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findByUserId(TEST_USER_ID)).thenReturn(progressList);

        // Act
        List<ProgressDTO> result = progressService.getProgressByUserId(TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        verify(progressRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return progress by module id")
    void getProgressByModuleId_shouldReturnProgress() {
        // Arrange
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findByModuleId(TEST_MODULE_ID)).thenReturn(progressList);

        // Act
        List<ProgressDTO> result = progressService.getProgressByModuleId(TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getModuleId()).isEqualTo(TEST_MODULE_ID);
        verify(progressRepository).findByModuleId(TEST_MODULE_ID);
    }

    @Test
    @DisplayName("Should return progress by user id and module id")
    void getProgressByUserIdAndModuleId_shouldReturnProgress() {
        // Arrange
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID))
            .thenReturn(Optional.of(testProgress));

        // Act
        ProgressDTO result = progressService.getProgressByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getModuleId()).isEqualTo(TEST_MODULE_ID);
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);
    }

    @Test
    @DisplayName("Should throw exception when progress by user id and module id not found")
    void getProgressByUserIdAndModuleId_whenProgressNotFound_shouldThrowException() {
        // Arrange
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, 999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, 
            () -> progressService.getProgressByUserIdAndModuleId(TEST_USER_ID, 999L));
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, 999L);
    }

    @Test
    @DisplayName("Should mark module as completed")
    void markModuleAsCompleted_shouldReturnCompletedProgress() {
        // Arrange
        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID))
            .thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenReturn(testProgress);

        // Act
        ProgressDTO result = progressService.markModuleAsCompleted(TEST_USER_ID, TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getModuleId()).isEqualTo(TEST_MODULE_ID);
        assertThat(result.isCompleted()).isTrue();
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should update progress when already exists")
    void markModuleAsCompleted_whenProgressExists_shouldUpdateProgress() {
        // Arrange
        Progress existingProgress = new Progress();
        existingProgress.setId(1L);
        existingProgress.setUserId(TEST_USER_ID);
        existingProgress.setModule(testModule);
        existingProgress.setCompleted(false);

        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID))
            .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any(Progress.class))).thenReturn(testProgress);

        // Act
        ProgressDTO result = progressService.markModuleAsCompleted(TEST_USER_ID, TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isCompleted()).isTrue();
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent module as completed")
    void markModuleAsCompleted_whenModuleNotFound_shouldThrowException() {
        // Arrange
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, 
            () -> progressService.markModuleAsCompleted(TEST_USER_ID, 999L));
        verify(moduleRepository).findById(999L);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should reset module progress")
    void resetModuleProgress_shouldReturnResetProgress() {
        // Arrange
        Progress existingProgress = new Progress();
        existingProgress.setId(1L);
        existingProgress.setUserId(TEST_USER_ID);
        existingProgress.setModule(testModule);
        existingProgress.setCompleted(true);
        existingProgress.setCompletedAt(LocalDateTime.now());

        Progress resetProgress = new Progress();
        resetProgress.setId(1L);
        resetProgress.setUserId(TEST_USER_ID);
        resetProgress.setModule(testModule);
        resetProgress.setCompleted(false);
        resetProgress.setCompletedAt(null);

        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID))
            .thenReturn(Optional.of(existingProgress));
        when(progressRepository.save(any(Progress.class))).thenReturn(resetProgress);

        // Act
        ProgressDTO result = progressService.resetModuleProgress(TEST_USER_ID, TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isCompleted()).isFalse();
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should throw exception when resetting progress for non-existent module")
    void resetModuleProgress_whenModuleNotFound_shouldThrowException() {
        // Arrange
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, 
            () -> progressService.resetModuleProgress(TEST_USER_ID, 999L));
        verify(moduleRepository).findById(999L);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should create new progress record when resetting non-existent progress")
    void resetModuleProgress_whenProgressNotFound_shouldCreateNewProgress() {
        // Arrange
        Progress newProgress = new Progress();
        newProgress.setId(1L);
        newProgress.setUserId(TEST_USER_ID);
        newProgress.setModule(testModule);
        newProgress.setCompleted(false);

        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(progressRepository.findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID))
            .thenReturn(Optional.empty());
        when(progressRepository.save(any(Progress.class))).thenReturn(newProgress);

        // Act
        ProgressDTO result = progressService.resetModuleProgress(TEST_USER_ID, TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isCompleted()).isFalse();
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(progressRepository).findByUserIdAndModuleId(TEST_USER_ID, TEST_MODULE_ID);
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    @DisplayName("Should delete progress")
    void deleteProgress_shouldDeleteProgress() {
        // Arrange
        when(progressRepository.existsById(1L)).thenReturn(true);
        doNothing().when(progressRepository).deleteById(1L);

        // Act
        progressService.deleteProgress(1L);

        // Assert
        verify(progressRepository).existsById(1L);
        verify(progressRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent progress")
    void deleteProgress_whenProgressNotFound_shouldThrowException() {
        // Arrange
        when(progressRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> progressService.deleteProgress(999L));
        verify(progressRepository).existsById(999L);
        verify(progressRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get course progress stats")
    void getCourseProgressStats_shouldReturnStats() {
        // Arrange
        long completedModules = 5;
        when(progressRepository.countCompletedModulesByCourseAndUser(TEST_USER_ID, TEST_COURSE_ID))
            .thenReturn(completedModules);
        // This is a placeholder for however your service counts total modules per course
        // For now we'll use a mock approach
        when(moduleRepository.findByCourseId(TEST_COURSE_ID)).thenReturn(Arrays.asList(
            testModule, testModule, testModule, testModule, testModule,
            testModule, testModule, testModule, testModule, testModule
        ));

        // Act
        Map<String, Object> result = progressService.getCourseProgressStats(TEST_USER_ID, TEST_COURSE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsKey("totalModules");
        assertThat(result).containsKey("completedModules");
        assertThat(result).containsKey("completionPercentage");
        verify(progressRepository).countCompletedModulesByCourseAndUser(TEST_USER_ID, TEST_COURSE_ID);
    }
}
