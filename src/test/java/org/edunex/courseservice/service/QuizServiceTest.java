package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizDTO;
import org.edunex.courseservice.dto.QuizQuestionDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private QuizService quizService;

    private Quiz testQuiz;
    private Module testModule;
    private final Long TEST_MODULE_ID = 1L;
    private final Long TEST_QUIZ_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test module
        testModule = new Module();
        testModule.setId(TEST_MODULE_ID);
        testModule.setTitle("Test Module");
        
        // Setup test quiz
        testQuiz = new Quiz();
        testQuiz.setId(TEST_QUIZ_ID);
        testQuiz.setTitle("Test Quiz");
        testQuiz.setModule(testModule);
        testQuiz.setQuestions(new ArrayList<>());
        
        testModule.setQuiz(testQuiz);
    }

    @Test
    @DisplayName("Should return all quizzes")
    void getAllQuizzes_shouldReturnAllQuizzes() {
        // Arrange
        List<Quiz> quizzes = Arrays.asList(testQuiz);
        when(quizRepository.findAll()).thenReturn(quizzes);

        // Act
        List<QuizDTO> result = quizService.getAllQuizzes();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Quiz");
        verify(quizRepository).findAll();
    }

    @Test
    @DisplayName("Should return quiz by id")
    void getQuizById_shouldReturnQuiz() {
        // Arrange
        when(quizRepository.findById(TEST_QUIZ_ID)).thenReturn(Optional.of(testQuiz));

        // Act
        QuizDTO result = quizService.getQuizById(TEST_QUIZ_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_QUIZ_ID);
        assertThat(result.getTitle()).isEqualTo("Test Quiz");
        verify(quizRepository).findById(TEST_QUIZ_ID);
    }

    @Test
    @DisplayName("Should throw exception when quiz not found")
    void getQuizById_whenQuizNotFound_shouldThrowException() {
        // Arrange
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizService.getQuizById(999L));
        verify(quizRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return quizzes by module id")
    void getQuizzesByModuleId_shouldReturnQuizzes() {
        // Arrange
        List<Quiz> quizzes = Arrays.asList(testQuiz);
        when(quizRepository.findByModuleId(TEST_MODULE_ID)).thenReturn(quizzes);

        // Act
        List<QuizDTO> result = quizService.getQuizzesByModuleId(TEST_MODULE_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getModuleId()).isEqualTo(TEST_MODULE_ID);
        verify(quizRepository).findByModuleId(TEST_MODULE_ID);
    }

    @Test
    @DisplayName("Should create a new quiz")
    void createQuiz_shouldReturnCreatedQuiz() {
        // Arrange
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setTitle("New Quiz");
        quizDTO.setModuleId(TEST_MODULE_ID);
        quizDTO.setQuestions(new ArrayList<>());

        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        // Act
        QuizDTO result = quizService.createQuiz(quizDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_QUIZ_ID);
        assertThat(result.getTitle()).isEqualTo("Test Quiz");
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should throw exception when creating quiz for non-existent module")
    void createQuiz_whenModuleNotFound_shouldThrowException() {
        // Arrange
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setTitle("New Quiz");
        quizDTO.setModuleId(999L);

        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizService.createQuiz(quizDTO));
        verify(moduleRepository).findById(999L);
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should create quiz with questions")
    void createQuiz_withQuestions_shouldReturnCreatedQuizWithQuestions() {
        // Arrange
        QuizQuestionDTO questionDTO = new QuizQuestionDTO();
        questionDTO.setQuestionText("Test Question");
        questionDTO.setAnswers(new ArrayList<>());
        
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setTitle("New Quiz with Questions");
        quizDTO.setModuleId(TEST_MODULE_ID);
        quizDTO.setQuestions(Arrays.asList(questionDTO));

        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        // Act
        QuizDTO result = quizService.createQuiz(quizDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should update an existing quiz")
    void updateQuiz_shouldReturnUpdatedQuiz() {
        // Arrange
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setTitle("Updated Quiz");
        quizDTO.setModuleId(TEST_MODULE_ID);
        quizDTO.setQuestions(new ArrayList<>());

        Quiz updatedQuiz = new Quiz();
        updatedQuiz.setId(TEST_QUIZ_ID);
        updatedQuiz.setTitle("Updated Quiz");
        updatedQuiz.setModule(testModule);
        updatedQuiz.setQuestions(new ArrayList<>());

        when(quizRepository.findById(TEST_QUIZ_ID)).thenReturn(Optional.of(testQuiz));
        when(moduleRepository.findById(TEST_MODULE_ID)).thenReturn(Optional.of(testModule));
        when(quizRepository.save(any(Quiz.class))).thenReturn(updatedQuiz);

        // Act
        QuizDTO result = quizService.updateQuiz(TEST_QUIZ_ID, quizDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_QUIZ_ID);
        assertThat(result.getTitle()).isEqualTo("Updated Quiz");
        verify(quizRepository).findById(TEST_QUIZ_ID);
        verify(moduleRepository).findById(TEST_MODULE_ID);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent quiz")
    void updateQuiz_whenQuizNotFound_shouldThrowException() {
        // Arrange
        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setTitle("Updated Quiz");

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizService.updateQuiz(999L, quizDTO));
        verify(quizRepository).findById(999L);
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("Should delete quiz")
    void deleteQuiz_shouldDeleteQuiz() {
        // Arrange
        when(quizRepository.existsById(TEST_QUIZ_ID)).thenReturn(true);
        doNothing().when(quizRepository).deleteById(TEST_QUIZ_ID);

        // Act
        quizService.deleteQuiz(TEST_QUIZ_ID);

        // Assert
        verify(quizRepository).existsById(TEST_QUIZ_ID);
        verify(quizRepository).deleteById(TEST_QUIZ_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent quiz")
    void deleteQuiz_whenQuizNotFound_shouldThrowException() {
        // Arrange
        when(quizRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizService.deleteQuiz(999L));
        verify(quizRepository).existsById(999L);
        verify(quizRepository, never()).deleteById(anyLong());
    }
}
