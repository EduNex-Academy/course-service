package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizQuestionDTO;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.QuizQuestionRepository;
import org.edunex.courseservice.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-questions")
public class QuizQuestionController {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @GetMapping
    public ResponseEntity<List<QuizQuestionDTO>> getAllQuizQuestions() {
        List<QuizQuestion> questions = quizQuestionRepository.findAll();
        List<QuizQuestionDTO> questionDTOs = mapToQuizQuestionDTOs(questions);
        return ResponseEntity.ok(questionDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizQuestionDTO> getQuizQuestionById(@PathVariable Long id) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));

        return ResponseEntity.ok(mapToQuizQuestionDTO(question));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizQuestionDTO>> getQuizQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizId(quizId);
        List<QuizQuestionDTO> questionDTOs = mapToQuizQuestionDTOs(questions);
        return ResponseEntity.ok(questionDTOs);
    }

    @PostMapping
    public ResponseEntity<QuizQuestionDTO> createQuizQuestion(@RequestBody QuizQuestionDTO questionDTO) {
        Quiz quiz = quizRepository.findById(questionDTO.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        QuizQuestion question = new QuizQuestion();
        question.setQuestionText(questionDTO.getQuestionText());
        question.setQuiz(quiz);
        question.setAnswers(new ArrayList<>());

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);
        return new ResponseEntity<>(mapToQuizQuestionDTO(savedQuestion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizQuestionDTO> updateQuizQuestion(@PathVariable Long id, @RequestBody QuizQuestionDTO questionDTO) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));

        question.setQuestionText(questionDTO.getQuestionText());

        // If quizId is being changed, update the quiz reference
        if (!question.getQuiz().getId().equals(questionDTO.getQuizId())) {
            Quiz newQuiz = quizRepository.findById(questionDTO.getQuizId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
            question.setQuiz(newQuiz);
        }

        QuizQuestion updatedQuestion = quizQuestionRepository.save(question);
        return ResponseEntity.ok(mapToQuizQuestionDTO(updatedQuestion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizQuestion(@PathVariable Long id) {
        if (!quizQuestionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found");
        }

        quizQuestionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
    private QuizQuestionDTO mapToQuizQuestionDTO(QuizQuestion question) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());

        if (question.getQuiz() != null) {
            dto.setQuizId(question.getQuiz().getId());
        }

        // We don't map answers here to avoid circular references
        // They will be loaded separately when needed

        return dto;
    }

    private List<QuizQuestionDTO> mapToQuizQuestionDTOs(List<QuizQuestion> questions) {
        return questions.stream()
                .map(this::mapToQuizQuestionDTO)
                .collect(Collectors.toList());
    }
}
