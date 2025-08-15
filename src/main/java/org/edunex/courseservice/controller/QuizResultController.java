package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizResultDTO;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizResult;
import org.edunex.courseservice.repository.QuizRepository;
import org.edunex.courseservice.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-results")
public class QuizResultController {

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuizRepository quizRepository;

    @GetMapping
    public ResponseEntity<List<QuizResultDTO>> getAllQuizResults() {
        List<QuizResult> results = quizResultRepository.findAll();
        List<QuizResultDTO> resultDTOs = mapToQuizResultDTOs(results);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResultDTO> getQuizResultById(@PathVariable Long id) {
        QuizResult result = quizResultRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz result not found"));

        return ResponseEntity.ok(mapToQuizResultDTO(result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizResultDTO>> getQuizResultsByUserId(@PathVariable String userId) {
        List<QuizResult> results = quizResultRepository.findByUserId(userId);
        List<QuizResultDTO> resultDTOs = mapToQuizResultDTOs(results);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizResultDTO>> getQuizResultsByQuizId(@PathVariable Long quizId) {
        List<QuizResult> results = quizResultRepository.findByQuizId(quizId);
        List<QuizResultDTO> resultDTOs = mapToQuizResultDTOs(results);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/user/{userId}/quiz/{quizId}")
    public ResponseEntity<List<QuizResultDTO>> getQuizResultsByUserIdAndQuizId(
            @PathVariable String userId,
            @PathVariable Long quizId) {
        List<QuizResult> results = quizResultRepository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(userId, quizId);
        List<QuizResultDTO> resultDTOs = mapToQuizResultDTOs(results);
        return ResponseEntity.ok(resultDTOs);
    }

    @GetMapping("/user/{userId}/quiz/{quizId}/best")
    public ResponseEntity<QuizResultDTO> getBestQuizResultForUser(
            @PathVariable String userId,
            @PathVariable Long quizId) {
        QuizResult result = quizResultRepository.findTopByUserIdAndQuizIdOrderByScoreDesc(userId, quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz result found"));

        return ResponseEntity.ok(mapToQuizResultDTO(result));
    }

    @PostMapping
    public ResponseEntity<QuizResultDTO> createQuizResult(@RequestBody QuizResultDTO quizResultDTO) {
        Quiz quiz = quizRepository.findById(quizResultDTO.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        QuizResult quizResult = new QuizResult();
        quizResult.setUserId(quizResultDTO.getUserId());
        quizResult.setQuiz(quiz);
        quizResult.setScore(quizResultDTO.getScore());
        quizResult.setSubmittedAt(LocalDateTime.now());

        QuizResult savedResult = quizResultRepository.save(quizResult);
        return new ResponseEntity<>(mapToQuizResultDTO(savedResult), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuizResult(@PathVariable Long id) {
        if (!quizResultRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz result not found");
        }

        quizResultRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
    private QuizResultDTO mapToQuizResultDTO(QuizResult quizResult) {
        QuizResultDTO dto = new QuizResultDTO();
        dto.setId(quizResult.getId());
        dto.setUserId(quizResult.getUserId());
        dto.setQuizId(quizResult.getQuiz().getId());
        dto.setQuizTitle(quizResult.getQuiz().getTitle());
        dto.setScore(quizResult.getScore());
        dto.setSubmittedAt(quizResult.getSubmittedAt());

        // Set additional fields if they are accessible
        if (quizResult.getQuiz().getModule() != null) {
            dto.setModuleTitle(quizResult.getQuiz().getModule().getTitle());

            if (quizResult.getQuiz().getModule().getCourse() != null) {
                dto.setCourseId(quizResult.getQuiz().getModule().getCourse().getId());
                dto.setCourseTitle(quizResult.getQuiz().getModule().getCourse().getTitle());
            }
        }

        return dto;
    }

    private List<QuizResultDTO> mapToQuizResultDTOs(List<QuizResult> quizResults) {
        return quizResults.stream()
                .map(this::mapToQuizResultDTO)
                .collect(Collectors.toList());
    }
}
