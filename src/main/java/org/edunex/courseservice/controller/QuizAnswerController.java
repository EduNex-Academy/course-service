package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizAnswerDTO;
import org.edunex.courseservice.model.QuizAnswer;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.QuizAnswerRepository;
import org.edunex.courseservice.repository.QuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-answers")
public class QuizAnswerController {

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @GetMapping
    public ResponseEntity<List<QuizAnswerDTO>> getAllQuizAnswers() {
        List<QuizAnswer> answers = quizAnswerRepository.findAll();
        List<QuizAnswerDTO> answerDTOs = mapToQuizAnswerDTOs(answers);
        return ResponseEntity.ok(answerDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAnswerDTO> getQuizAnswerById(@PathVariable Long id) {
        QuizAnswer answer = quizAnswerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz answer not found"));

        return ResponseEntity.ok(mapToQuizAnswerDTO(answer));
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<QuizAnswerDTO>> getQuizAnswersByQuestionId(@PathVariable Long questionId) {
        List<QuizAnswer> answers = quizAnswerRepository.findByQuestionId(questionId);
        List<QuizAnswerDTO> answerDTOs = mapToQuizAnswerDTOs(answers);
        return ResponseEntity.ok(answerDTOs);
    }

    @GetMapping("/question/{questionId}/correct")
    public ResponseEntity<List<QuizAnswerDTO>> getCorrectQuizAnswersByQuestionId(@PathVariable Long questionId) {
        List<QuizAnswer> answers = quizAnswerRepository.findByQuestionIdAndIsCorrect(questionId, true);
        List<QuizAnswerDTO> answerDTOs = mapToQuizAnswerDTOs(answers);
        return ResponseEntity.ok(answerDTOs);
    }

    @PostMapping
    public ResponseEntity<QuizAnswerDTO> createQuizAnswer(@RequestBody QuizAnswerDTO answerDTO) {
        QuizQuestion question = quizQuestionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));

        QuizAnswer answer = new QuizAnswer();
        answer.setAnswerText(answerDTO.getAnswerText());
        answer.setCorrect(answerDTO.isCorrect());
        answer.setQuestion(question);

        QuizAnswer savedAnswer = quizAnswerRepository.save(answer);
        return new ResponseEntity<>(mapToQuizAnswerDTO(savedAnswer), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizAnswerDTO> updateQuizAnswer(@PathVariable Long id, @RequestBody QuizAnswerDTO answerDTO) {
        QuizAnswer answer = quizAnswerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz answer not found"));

        answer.setAnswerText(answerDTO.getAnswerText());
        answer.setCorrect(answerDTO.isCorrect());

        // If questionId is being changed, update the question reference
        if (!answer.getQuestion().getId().equals(answerDTO.getQuestionId())) {
            QuizQuestion newQuestion = quizQuestionRepository.findById(answerDTO.getQuestionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));
            answer.setQuestion(newQuestion);
        }

        QuizAnswer updatedAnswer = quizAnswerRepository.save(answer);
        return ResponseEntity.ok(mapToQuizAnswerDTO(updatedAnswer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizAnswer(@PathVariable Long id) {
        if (!quizAnswerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz answer not found");
        }

        quizAnswerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
    private QuizAnswerDTO mapToQuizAnswerDTO(QuizAnswer answer) {
        QuizAnswerDTO dto = new QuizAnswerDTO();
        dto.setId(answer.getId());
        dto.setAnswerText(answer.getAnswerText());
        dto.setCorrect(answer.isCorrect());

        if (answer.getQuestion() != null) {
            dto.setQuestionId(answer.getQuestion().getId());
        }

        return dto;
    }

    private List<QuizAnswerDTO> mapToQuizAnswerDTOs(List<QuizAnswer> answers) {
        return answers.stream()
                .map(this::mapToQuizAnswerDTO)
                .collect(Collectors.toList());
    }
}
