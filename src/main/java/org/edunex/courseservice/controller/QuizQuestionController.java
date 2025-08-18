package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizQuestionDTO;
import org.edunex.courseservice.service.QuizQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-questions")
public class QuizQuestionController {

    @Autowired
    private QuizQuestionService quizQuestionService;

    @GetMapping
    public ResponseEntity<List<QuizQuestionDTO>> getAllQuizQuestions() {
        List<QuizQuestionDTO> questionDTOs = quizQuestionService.getAllQuizQuestions();
        return ResponseEntity.ok(questionDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizQuestionDTO> getQuizQuestionById(@PathVariable Long id) {
        QuizQuestionDTO questionDTO = quizQuestionService.getQuizQuestionById(id);
        return ResponseEntity.ok(questionDTO);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizQuestionDTO>> getQuizQuestionsByQuizId(@PathVariable Long quizId) {
        List<QuizQuestionDTO> questionDTOs = quizQuestionService.getQuizQuestionsByQuizId(quizId);
        return ResponseEntity.ok(questionDTOs);
    }

    @PostMapping
    public ResponseEntity<QuizQuestionDTO> createQuizQuestion(@RequestBody QuizQuestionDTO questionDTO) {
        QuizQuestionDTO createdQuestion = quizQuestionService.createQuizQuestion(questionDTO);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizQuestionDTO> updateQuizQuestion(@PathVariable Long id, @RequestBody QuizQuestionDTO questionDTO) {
        QuizQuestionDTO updatedQuestion = quizQuestionService.updateQuizQuestion(id, questionDTO);
        return ResponseEntity.ok(updatedQuestion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizQuestion(@PathVariable Long id) {
        quizQuestionService.deleteQuizQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
