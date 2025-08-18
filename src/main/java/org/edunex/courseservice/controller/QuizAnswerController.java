package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizAnswerDTO;
import org.edunex.courseservice.service.QuizAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-answers")
public class QuizAnswerController {

    @Autowired
    private QuizAnswerService quizAnswerService;

    @GetMapping
    public ResponseEntity<List<QuizAnswerDTO>> getAllQuizAnswers() {
        List<QuizAnswerDTO> answerDTOs = quizAnswerService.getAllQuizAnswers();
        return ResponseEntity.ok(answerDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAnswerDTO> getQuizAnswerById(@PathVariable Long id) {
        QuizAnswerDTO answerDTO = quizAnswerService.getQuizAnswerById(id);
        return ResponseEntity.ok(answerDTO);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<QuizAnswerDTO>> getQuizAnswersByQuestionId(@PathVariable Long questionId) {
        List<QuizAnswerDTO> answerDTOs = quizAnswerService.getQuizAnswersByQuestionId(questionId);
        return ResponseEntity.ok(answerDTOs);
    }

    @GetMapping("/question/{questionId}/correct")
    public ResponseEntity<List<QuizAnswerDTO>> getCorrectQuizAnswersByQuestionId(@PathVariable Long questionId) {
        List<QuizAnswerDTO> answerDTOs = quizAnswerService.getCorrectQuizAnswersByQuestionId(questionId);
        return ResponseEntity.ok(answerDTOs);
    }

    @PostMapping
    public ResponseEntity<QuizAnswerDTO> createQuizAnswer(@RequestBody QuizAnswerDTO answerDTO) {
        QuizAnswerDTO createdAnswer = quizAnswerService.createQuizAnswer(answerDTO);
        return new ResponseEntity<>(createdAnswer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizAnswerDTO> updateQuizAnswer(@PathVariable Long id, @RequestBody QuizAnswerDTO answerDTO) {
        QuizAnswerDTO updatedAnswer = quizAnswerService.updateQuizAnswer(id, answerDTO);
        return ResponseEntity.ok(updatedAnswer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizAnswer(@PathVariable Long id) {
        quizAnswerService.deleteQuizAnswer(id);
        return ResponseEntity.noContent().build();
    }
}
