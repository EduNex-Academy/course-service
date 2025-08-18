package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizResultDTO;
import org.edunex.courseservice.service.QuizResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-results")
public class QuizResultController {

    @Autowired
    private QuizResultService quizResultService;

    @GetMapping
    public ResponseEntity<List<QuizResultDTO>> getAllQuizResults() {
        List<QuizResultDTO> resultDTOs = quizResultService.getAllQuizResults();
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResultDTO> getQuizResultById(@PathVariable Long id) {
        QuizResultDTO resultDTO = quizResultService.getQuizResultById(id);
        return ResponseEntity.ok(resultDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizResultDTO>> getQuizResultsByUserId(@PathVariable String userId) {
        List<QuizResultDTO> resultDTOs = quizResultService.getQuizResultsByUserId(userId);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizResultDTO>> getQuizResultsByQuizId(@PathVariable Long quizId) {
        List<QuizResultDTO> resultDTOs = quizResultService.getQuizResultsByQuizId(quizId);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/user/{userId}/quiz/{quizId}")
    public ResponseEntity<List<QuizResultDTO>> getQuizResultsByUserIdAndQuizId(
            @PathVariable String userId,
            @PathVariable Long quizId) {
        List<QuizResultDTO> resultDTOs = quizResultService.getQuizResultsByUserIdAndQuizId(userId, quizId);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/user/{userId}/quiz/{quizId}/best")
    public ResponseEntity<QuizResultDTO> getBestQuizResultForUser(
            @PathVariable String userId,
            @PathVariable Long quizId) {
        QuizResultDTO resultDTO = quizResultService.getBestQuizResultForUser(userId, quizId);
        return ResponseEntity.ok(resultDTO);
    }

    @PostMapping
    public ResponseEntity<QuizResultDTO> createQuizResult(@RequestBody QuizResultDTO quizResultDTO) {
        QuizResultDTO createdResult = quizResultService.createQuizResult(quizResultDTO);
        return new ResponseEntity<>(createdResult, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizResult(@PathVariable Long id) {
        quizResultService.deleteQuizResult(id);
        return ResponseEntity.noContent().build();
    }
}
