package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizQuestionDTO;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.QuizQuestionRepository;
import org.edunex.courseservice.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizQuestionService {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizRepository quizRepository;

    public List<QuizQuestionDTO> getAllQuizQuestions() {
        List<QuizQuestion> questions = quizQuestionRepository.findAll();
        return mapToQuizQuestionDTOs(questions);
    }

    public QuizQuestionDTO getQuizQuestionById(Long id) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));
        return mapToQuizQuestionDTO(question);
    }

    public List<QuizQuestionDTO> getQuizQuestionsByQuizId(Long quizId) {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        return mapToQuizQuestionDTOs(questions);
    }

    public QuizQuestionDTO createQuizQuestion(QuizQuestionDTO questionDTO) {
        Quiz quiz = quizRepository.findById(questionDTO.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        QuizQuestion question = new QuizQuestion();
        question.setQuestionText(questionDTO.getQuestionText());
        question.setQuiz(quiz);
        question.setAnswers(new ArrayList<>());

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);
        return mapToQuizQuestionDTO(savedQuestion);
    }

    public QuizQuestionDTO updateQuizQuestion(Long id, QuizQuestionDTO questionDTO) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));

        question.setQuestionText(questionDTO.getQuestionText());

        if (!question.getQuiz().getId().equals(questionDTO.getQuizId())) {
            Quiz newQuiz = quizRepository.findById(questionDTO.getQuizId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
            question.setQuiz(newQuiz);
        }

        QuizQuestion updatedQuestion = quizQuestionRepository.save(question);
        return mapToQuizQuestionDTO(updatedQuestion);
    }

    public void deleteQuizQuestion(Long id) {
        if (!quizQuestionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found");
        }
        quizQuestionRepository.deleteById(id);
    }

    private QuizQuestionDTO mapToQuizQuestionDTO(QuizQuestion question) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());

        if (question.getQuiz() != null) {
            dto.setQuizId(question.getQuiz().getId());
        }

        return dto;
    }

    private List<QuizQuestionDTO> mapToQuizQuestionDTOs(List<QuizQuestion> questions) {
        return questions.stream()
                .map(this::mapToQuizQuestionDTO)
                .collect(Collectors.toList());
    }
}
