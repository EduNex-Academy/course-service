package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizResultDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizResult;
import org.edunex.courseservice.repository.QuizRepository;
import org.edunex.courseservice.repository.QuizResultRepository;
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
class QuizResultServiceTest {

    @Mock
    private QuizResultRepository quizResultRepository;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizResultService quizResultService;

    private QuizResult testQuizResult;
    private Quiz testQuiz;
    private Module testModule;
    private final String TEST_USER_ID = "user-123";
    private final Long TEST_QUIZ_ID = 1L;
    private final Long TEST_RESULT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test module
        testModule = new Module();
        testModule.setId(1L);
        testModule.setTitle("Test Module");
        
        // Setup test quiz
        testQuiz = new Quiz();
        testQuiz.setId(TEST_QUIZ_ID);
        testQuiz.setTitle("Test Quiz");
        testQuiz.setModule(testModule);
        
        // Setup test quiz result
        testQuizResult = new QuizResult();
        testQuizResult.setId(TEST_RESULT_ID);
        testQuizResult.setUserId(TEST_USER_ID);
        testQuizResult.setQuiz(testQuiz);
        testQuizResult.setScore(80);
        testQuizResult.setSubmittedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return all quiz results")
    void getAllQuizResults_shouldReturnAllResults() {
        // Arrange
        List<QuizResult> results = Arrays.asList(testQuizResult);
        when(quizResultRepository.findAll()).thenReturn(results);

        // Act
        List<QuizResultDTO> resultDTOs = quizResultService.getAllQuizResults();

        // Assert
        assertThat(resultDTOs).isNotNull();
        assertThat(resultDTOs.size()).isEqualTo(1);
        assertThat(resultDTOs.get(0).getScore()).isEqualTo(80);
        verify(quizResultRepository).findAll();
    }

    @Test
    @DisplayName("Should return quiz result by id")
    void getQuizResultById_shouldReturnResult() {
        // Arrange
        when(quizResultRepository.findById(TEST_RESULT_ID)).thenReturn(Optional.of(testQuizResult));

        // Act
        QuizResultDTO result = quizResultService.getQuizResultById(TEST_RESULT_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_RESULT_ID);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        verify(quizResultRepository).findById(TEST_RESULT_ID);
    }

    @Test
    @DisplayName("Should throw exception when quiz result not found")
    void getQuizResultById_whenResultNotFound_shouldThrowException() {
        // Arrange
        when(quizResultRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizResultService.getQuizResultById(999L));
        verify(quizResultRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return quiz results by user id")
    void getQuizResultsByUserId_shouldReturnResults() {
        // Arrange
        List<QuizResult> results = Arrays.asList(testQuizResult);
        when(quizResultRepository.findByUserId(TEST_USER_ID)).thenReturn(results);

        // Act
        List<QuizResultDTO> resultDTOs = quizResultService.getQuizResultsByUserId(TEST_USER_ID);

        // Assert
        assertThat(resultDTOs).isNotNull();
        assertThat(resultDTOs.size()).isEqualTo(1);
        assertThat(resultDTOs.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        verify(quizResultRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return quiz results by quiz id")
    void getQuizResultsByQuizId_shouldReturnResults() {
        // Arrange
        List<QuizResult> results = Arrays.asList(testQuizResult);
        when(quizResultRepository.findByQuizId(TEST_QUIZ_ID)).thenReturn(results);

        // Act
        List<QuizResultDTO> resultDTOs = quizResultService.getQuizResultsByQuizId(TEST_QUIZ_ID);

        // Assert
        assertThat(resultDTOs).isNotNull();
        assertThat(resultDTOs.size()).isEqualTo(1);
        assertThat(resultDTOs.get(0).getQuizId()).isEqualTo(TEST_QUIZ_ID);
        verify(quizResultRepository).findByQuizId(TEST_QUIZ_ID);
    }

    @Test
    @DisplayName("Should return quiz results by user id and quiz id")
    void getQuizResultsByUserIdAndQuizId_shouldReturnResults() {
        // Arrange
        List<QuizResult> results = Arrays.asList(testQuizResult);
        when(quizResultRepository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(TEST_USER_ID, TEST_QUIZ_ID)).thenReturn(results);

        // Act
        List<QuizResultDTO> resultDTOs = quizResultService.getQuizResultsByUserIdAndQuizId(TEST_USER_ID, TEST_QUIZ_ID);

        // Assert
        assertThat(resultDTOs).isNotNull();
        assertThat(resultDTOs.size()).isEqualTo(1);
        assertThat(resultDTOs.get(0).getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(resultDTOs.get(0).getQuizId()).isEqualTo(TEST_QUIZ_ID);
        verify(quizResultRepository).findByUserIdAndQuizIdOrderBySubmittedAtDesc(TEST_USER_ID, TEST_QUIZ_ID);
    }

    @Test
    @DisplayName("Should return best quiz result for user")
    void getBestQuizResultForUser_shouldReturnBestResult() {
        // Arrange
        QuizResult bestResult = new QuizResult();
        bestResult.setId(2L);
        bestResult.setUserId(TEST_USER_ID);
        bestResult.setQuiz(testQuiz);
        bestResult.setScore(95);
        bestResult.setSubmittedAt(LocalDateTime.now());

        when(quizResultRepository.findTopByUserIdAndQuizIdOrderByScoreDesc(TEST_USER_ID, TEST_QUIZ_ID))
            .thenReturn(Optional.of(bestResult));

        // Act
        QuizResultDTO result = quizResultService.getBestQuizResultForUser(TEST_USER_ID, TEST_QUIZ_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getScore()).isEqualTo(95);
        verify(quizResultRepository).findTopByUserIdAndQuizIdOrderByScoreDesc(TEST_USER_ID, TEST_QUIZ_ID);
    }

    @Test
    @DisplayName("Should throw exception when no quiz result found for user and quiz")
    void getBestQuizResultForUser_whenResultNotFound_shouldThrowException() {
        // Arrange
        when(quizResultRepository.findTopByUserIdAndQuizIdOrderByScoreDesc(TEST_USER_ID, 999L))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, 
            () -> quizResultService.getBestQuizResultForUser(TEST_USER_ID, 999L));
        verify(quizResultRepository).findTopByUserIdAndQuizIdOrderByScoreDesc(TEST_USER_ID, 999L);
    }

    @Test
    @DisplayName("Should create a new quiz result")
    void createQuizResult_shouldReturnCreatedResult() {
        // Arrange
        QuizResultDTO quizResultDTO = new QuizResultDTO();
        quizResultDTO.setUserId(TEST_USER_ID);
        quizResultDTO.setQuizId(TEST_QUIZ_ID);
        quizResultDTO.setScore(90);

        when(quizRepository.findById(TEST_QUIZ_ID)).thenReturn(Optional.of(testQuiz));
        when(quizResultRepository.save(any(QuizResult.class))).thenReturn(testQuizResult);

        // Act
        QuizResultDTO result = quizResultService.createQuizResult(quizResultDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_RESULT_ID);
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        verify(quizRepository).findById(TEST_QUIZ_ID);
        verify(quizResultRepository).save(any(QuizResult.class));
    }

    @Test
    @DisplayName("Should throw exception when creating result for non-existent quiz")
    void createQuizResult_whenQuizNotFound_shouldThrowException() {
        // Arrange
        QuizResultDTO quizResultDTO = new QuizResultDTO();
        quizResultDTO.setUserId(TEST_USER_ID);
        quizResultDTO.setQuizId(999L);
        quizResultDTO.setScore(90);

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizResultService.createQuizResult(quizResultDTO));
        verify(quizRepository).findById(999L);
        verify(quizResultRepository, never()).save(any(QuizResult.class));
    }

    @Test
    @DisplayName("Should delete quiz result")
    void deleteQuizResult_shouldDeleteResult() {
        // Arrange
        when(quizResultRepository.existsById(TEST_RESULT_ID)).thenReturn(true);
        doNothing().when(quizResultRepository).deleteById(TEST_RESULT_ID);

        // Act
        quizResultService.deleteQuizResult(TEST_RESULT_ID);

        // Assert
        verify(quizResultRepository).existsById(TEST_RESULT_ID);
        verify(quizResultRepository).deleteById(TEST_RESULT_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent result")
    void deleteQuizResult_whenResultNotFound_shouldThrowException() {
        // Arrange
        when(quizResultRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizResultService.deleteQuizResult(999L));
        verify(quizResultRepository).existsById(999L);
        verify(quizResultRepository, never()).deleteById(anyLong());
    }
}
