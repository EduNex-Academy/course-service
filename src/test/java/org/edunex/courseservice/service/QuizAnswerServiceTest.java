package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizAnswerDTO;
import org.edunex.courseservice.model.QuizAnswer;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.QuizAnswerRepository;
import org.edunex.courseservice.repository.QuizQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizAnswerServiceTest {

    @Mock
    private QuizAnswerRepository quizAnswerRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @InjectMocks
    private QuizAnswerService quizAnswerService;

    private QuizAnswer testAnswer;
    private QuizQuestion testQuestion;
    private final Long TEST_QUESTION_ID = 1L;
    private final Long TEST_ANSWER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test question
        testQuestion = new QuizQuestion();
        testQuestion.setId(TEST_QUESTION_ID);
        testQuestion.setQuestionText("Test Question");
        
        // Setup test answer
        testAnswer = new QuizAnswer();
        testAnswer.setId(TEST_ANSWER_ID);
        testAnswer.setAnswerText("Test Answer");
        testAnswer.setCorrect(true);
        testAnswer.setQuestion(testQuestion);
    }

    @Test
    @DisplayName("Should return all quiz answers")
    void getAllQuizAnswers_shouldReturnAllAnswers() {
        // Arrange
        List<QuizAnswer> answers = Arrays.asList(testAnswer);
        when(quizAnswerRepository.findAll()).thenReturn(answers);

        // Act
        List<QuizAnswerDTO> result = quizAnswerService.getAllQuizAnswers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAnswerText()).isEqualTo("Test Answer");
        verify(quizAnswerRepository).findAll();
    }

    @Test
    @DisplayName("Should return quiz answer by id")
    void getQuizAnswerById_shouldReturnAnswer() {
        // Arrange
        when(quizAnswerRepository.findById(TEST_ANSWER_ID)).thenReturn(Optional.of(testAnswer));

        // Act
        QuizAnswerDTO result = quizAnswerService.getQuizAnswerById(TEST_ANSWER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ANSWER_ID);
        assertThat(result.getAnswerText()).isEqualTo("Test Answer");
        verify(quizAnswerRepository).findById(TEST_ANSWER_ID);
    }

    @Test
    @DisplayName("Should throw exception when quiz answer not found")
    void getQuizAnswerById_whenAnswerNotFound_shouldThrowException() {
        // Arrange
        when(quizAnswerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizAnswerService.getQuizAnswerById(999L));
        verify(quizAnswerRepository).findById(999L);
    }

    @Test
    @DisplayName("Should return quiz answers by question id")
    void getQuizAnswersByQuestionId_shouldReturnAnswers() {
        // Arrange
        List<QuizAnswer> answers = Arrays.asList(testAnswer);
        when(quizAnswerRepository.findByQuestionId(TEST_QUESTION_ID)).thenReturn(answers);

        // Act
        List<QuizAnswerDTO> result = quizAnswerService.getQuizAnswersByQuestionId(TEST_QUESTION_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getQuestionId()).isEqualTo(TEST_QUESTION_ID);
        verify(quizAnswerRepository).findByQuestionId(TEST_QUESTION_ID);
    }

    @Test
    @DisplayName("Should return correct quiz answers by question id")
    void getCorrectQuizAnswersByQuestionId_shouldReturnCorrectAnswers() {
        // Arrange
        List<QuizAnswer> answers = Arrays.asList(testAnswer);
        when(quizAnswerRepository.findByQuestionIdAndCorrect(TEST_QUESTION_ID, true)).thenReturn(answers);

        // Act
        List<QuizAnswerDTO> result = quizAnswerService.getCorrectQuizAnswersByQuestionId(TEST_QUESTION_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).isCorrect()).isTrue();
        verify(quizAnswerRepository).findByQuestionIdAndCorrect(TEST_QUESTION_ID, true);
    }

    @Test
    @DisplayName("Should create a new quiz answer")
    void createQuizAnswer_shouldReturnCreatedAnswer() {
        // Arrange
        QuizAnswerDTO answerDTO = new QuizAnswerDTO();
        answerDTO.setAnswerText("New Answer");
        answerDTO.setCorrect(true);
        answerDTO.setQuestionId(TEST_QUESTION_ID);

        when(quizQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(testQuestion));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(testAnswer);

        // Act
        QuizAnswerDTO result = quizAnswerService.createQuizAnswer(answerDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ANSWER_ID);
        assertThat(result.getAnswerText()).isEqualTo("Test Answer");
        verify(quizQuestionRepository).findById(TEST_QUESTION_ID);
        verify(quizAnswerRepository).save(any(QuizAnswer.class));
    }

    @Test
    @DisplayName("Should throw exception when creating answer for non-existent question")
    void createQuizAnswer_whenQuestionNotFound_shouldThrowException() {
        // Arrange
        QuizAnswerDTO answerDTO = new QuizAnswerDTO();
        answerDTO.setAnswerText("New Answer");
        answerDTO.setQuestionId(999L);

        when(quizQuestionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizAnswerService.createQuizAnswer(answerDTO));
        verify(quizQuestionRepository).findById(999L);
        verify(quizAnswerRepository, never()).save(any(QuizAnswer.class));
    }

    @Test
    @DisplayName("Should update an existing quiz answer")
    void updateQuizAnswer_shouldReturnUpdatedAnswer() {
        // Arrange
        QuizAnswerDTO answerDTO = new QuizAnswerDTO();
        answerDTO.setAnswerText("Updated Answer");
        answerDTO.setCorrect(false);
        answerDTO.setQuestionId(TEST_QUESTION_ID);

        QuizAnswer existingAnswer = testAnswer;
        QuizAnswer updatedAnswer = new QuizAnswer();
        updatedAnswer.setId(TEST_ANSWER_ID);
        updatedAnswer.setAnswerText("Updated Answer");
        updatedAnswer.setCorrect(false);
        updatedAnswer.setQuestion(testQuestion);

        when(quizAnswerRepository.findById(TEST_ANSWER_ID)).thenReturn(Optional.of(existingAnswer));
        when(quizQuestionRepository.findById(TEST_QUESTION_ID)).thenReturn(Optional.of(testQuestion));
        when(quizAnswerRepository.save(any(QuizAnswer.class))).thenReturn(updatedAnswer);

        // Act
        QuizAnswerDTO result = quizAnswerService.updateQuizAnswer(TEST_ANSWER_ID, answerDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ANSWER_ID);
        assertThat(result.getAnswerText()).isEqualTo("Updated Answer");
        assertThat(result.isCorrect()).isFalse();
        verify(quizAnswerRepository).findById(TEST_ANSWER_ID);
        verify(quizQuestionRepository).findById(TEST_QUESTION_ID);
        verify(quizAnswerRepository).save(any(QuizAnswer.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent answer")
    void updateQuizAnswer_whenAnswerNotFound_shouldThrowException() {
        // Arrange
        QuizAnswerDTO answerDTO = new QuizAnswerDTO();
        answerDTO.setAnswerText("Updated Answer");

        when(quizAnswerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizAnswerService.updateQuizAnswer(999L, answerDTO));
        verify(quizAnswerRepository).findById(999L);
        verify(quizAnswerRepository, never()).save(any(QuizAnswer.class));
    }

    @Test
    @DisplayName("Should delete quiz answer")
    void deleteQuizAnswer_shouldDeleteAnswer() {
        // Arrange
        when(quizAnswerRepository.existsById(TEST_ANSWER_ID)).thenReturn(true);
        doNothing().when(quizAnswerRepository).deleteById(TEST_ANSWER_ID);

        // Act
        quizAnswerService.deleteQuizAnswer(TEST_ANSWER_ID);

        // Assert
        verify(quizAnswerRepository).existsById(TEST_ANSWER_ID);
        verify(quizAnswerRepository).deleteById(TEST_ANSWER_ID);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent answer")
    void deleteQuizAnswer_whenAnswerNotFound_shouldThrowException() {
        // Arrange
        when(quizAnswerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> quizAnswerService.deleteQuizAnswer(999L));
        verify(quizAnswerRepository).existsById(999L);
        verify(quizAnswerRepository, never()).deleteById(anyLong());
    }
}
