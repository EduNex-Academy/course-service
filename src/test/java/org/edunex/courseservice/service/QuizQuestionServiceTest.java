package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizQuestionDTO;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.QuizQuestionRepository;
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
class QuizQuestionServiceTest {

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private QuizQuestionService quizQuestionService;

    private QuizQuestion testQuestion;
    private Quiz testQuiz;
    private final Long TEST_QUIZ_ID = 1L;
    private final Long TEST_QUESTION_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test quiz
        testQuiz = new Quiz();
        testQuiz.setId(TEST_QUIZ_ID);
        testQuiz.setTitle("Test Quiz");
        testQuiz.setQuestions(new ArrayList<>());
        
        // Setup test question
        testQuestion = new QuizQuestion();
        testQuestion.setId(TEST_QUESTION_ID);
        testQuestion.setQuestionText("Test Question");
        testQuestion.setQuiz(testQuiz);
        testQuestion.setAnswers(new ArrayList<>());
        
        testQuiz.getQuestions().add(testQuestion);
    }

    @Test
    @DisplayName("Should return all quiz questions")
    void getAllQuizQuestions_shouldReturnAllQuestions() {
        // Arrange
        List<QuizQuestion> questions = Arrays.asList(testQuestion);
        when(quizQuestionRepository.findAll()).thenReturn(questions);

        // Act
        List<QuizQuestionDTO> result = quizQuestionService.getAllQuizQuestions();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getQuestionText()).isEqualTo("Test Question");
        verify(quizQuestionRepository).findAll();
    }

    @Test
    @DisplayName("Should return quiz question by id")
    void getQuizQuestionById_shouldReturnQuestion() {
        // Arrange
        when(quizQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(testQuestion));

        // Act
        QuizQuestionDTO result = quizQuestionService.getQuizQuestionById(TEST_QUESTION_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_QUESTION_ID);
        assertThat(result.getQuestionText()).isEqualTo("Test Question");
        verify(quizQuestionRepository).findById(TEST_QUESTION_ID);
    }

    @Test
    @DisplayName("Should throw exception when quiz question not found")
    void getQuizQuestionById_whenQuestionNotFound_shouldThrowException() {
        // Arrange
        when(quizQuestionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizQuestionService.getQuizQuestionById(999L));
        verify(quizQuestionRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return quiz questions by quiz id")
    void getQuizQuestionsByQuizId_shouldReturnQuestions() {
        // Arrange
        List<QuizQuestion> questions = Arrays.asList(testQuestion);
        when(quizQuestionRepository.findByQuizId(TEST_QUIZ_ID)).thenReturn(questions);

        // Act
        List<QuizQuestionDTO> result = quizQuestionService.getQuizQuestionsByQuizId(TEST_QUIZ_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getQuizId()).isEqualTo(TEST_QUIZ_ID);
        verify(quizQuestionRepository).findByQuizId(TEST_QUIZ_ID);
    }

    @Test
    @DisplayName("Should create a new quiz question")
    void createQuizQuestion_shouldReturnCreatedQuestion() {
        // Arrange
        QuizQuestionDTO questionDTO = new QuizQuestionDTO();
        questionDTO.setQuestionText("New Question");
        questionDTO.setQuizId(TEST_QUIZ_ID);

        QuizQuestion newQuestion = new QuizQuestion();
        newQuestion.setId(2L);
        newQuestion.setQuestionText("New Question");
        newQuestion.setQuiz(testQuiz);
        newQuestion.setAnswers(new ArrayList<>());

        when(quizRepository.findById(TEST_QUIZ_ID)).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(newQuestion);

        // Act
        QuizQuestionDTO result = quizQuestionService.createQuizQuestion(questionDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getQuestionText()).isEqualTo("New Question");
        verify(quizRepository).findById(TEST_QUIZ_ID);
        verify(quizQuestionRepository).save(any(QuizQuestion.class));
    }

    @Test
    @DisplayName("Should throw exception when creating question for non-existent quiz")
    void createQuizQuestion_whenQuizNotFound_shouldThrowException() {
        // Arrange
        QuizQuestionDTO questionDTO = new QuizQuestionDTO();
        questionDTO.setQuestionText("New Question");
        questionDTO.setQuizId(999L);

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizQuestionService.createQuizQuestion(questionDTO));
        verify(quizRepository).findById(999L);
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    @DisplayName("Should update an existing quiz question")
    void updateQuizQuestion_shouldReturnUpdatedQuestion() {
        // Arrange
        QuizQuestionDTO questionDTO = new QuizQuestionDTO();
        questionDTO.setQuestionText("Updated Question");
        questionDTO.setQuizId(TEST_QUIZ_ID);

        QuizQuestion existingQuestion = testQuestion;
        QuizQuestion updatedQuestion = new QuizQuestion();
        updatedQuestion.setId(TEST_QUESTION_ID);
        updatedQuestion.setQuestionText("Updated Question");
        updatedQuestion.setQuiz(testQuiz);
        updatedQuestion.setAnswers(new ArrayList<>());

        when(quizQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(existingQuestion));
        when(quizRepository.findById(TEST_QUIZ_ID)).thenReturn(Optional.of(testQuiz));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(updatedQuestion);

        // Act
        QuizQuestionDTO result = quizQuestionService.updateQuizQuestion(TEST_QUESTION_ID, questionDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_QUESTION_ID);
        assertThat(result.getQuestionText()).isEqualTo("Updated Question");
        verify(quizQuestionRepository).findById(TEST_QUESTION_ID);
        verify(quizRepository).findById(TEST_QUIZ_ID);
        verify(quizQuestionRepository).save(any(QuizQuestion.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent question")
    void updateQuizQuestion_whenQuestionNotFound_shouldThrowException() {
        // Arrange
        QuizQuestionDTO questionDTO = new QuizQuestionDTO();
        questionDTO.setQuestionText("Updated Question");

        when(quizQuestionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizQuestionService.updateQuizQuestion(999L, questionDTO));
        verify(quizQuestionRepository).findById(999L);
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    @DisplayName("Should delete quiz question")
    void deleteQuizQuestion_shouldDeleteQuestion() {
        // Arrange
        when(quizQuestionRepository.existsById(TEST_QUESTION_ID)).thenReturn(true);
        doNothing().when(quizQuestionRepository).deleteById(TEST_QUESTION_ID);

        // Act
        quizQuestionService.deleteQuizQuestion(TEST_QUESTION_ID);

        // Assert
        verify(quizQuestionRepository).existsById(TEST_QUESTION_ID);
        verify(quizQuestionRepository).deleteById(TEST_QUESTION_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent question")
    void deleteQuizQuestion_whenQuestionNotFound_shouldThrowException() {
        // Arrange
        when(quizQuestionRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizQuestionService.deleteQuizQuestion(999L));
        verify(quizQuestionRepository).existsById(999L);
        verify(quizQuestionRepository, never()).deleteById(anyLong());
    }
}
