package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.QuizDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.ModuleRepository;
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
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @GetMapping
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        List<Quiz> quizzes = quizRepository.findAll();
        List<QuizDTO> quizDTOs = mapToQuizDTOs(quizzes);
        return ResponseEntity.ok(quizDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        return ResponseEntity.ok(mapToQuizDTO(quiz));
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesByModuleId(@PathVariable Long moduleId) {
        List<Quiz> quizzes = quizRepository.findByModuleId(moduleId);
        List<QuizDTO> quizDTOs = mapToQuizDTOs(quizzes);
        return ResponseEntity.ok(quizDTOs);
    }

    @PostMapping
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody QuizDTO quizDTO) {
        Module module = moduleRepository.findById(quizDTO.getModuleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setModule(module);
        quiz.setQuestions(new ArrayList<>());

        Quiz savedQuiz = quizRepository.save(quiz);
        return new ResponseEntity<>(mapToQuizDTO(savedQuiz), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Long id, @RequestBody QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        quiz.setTitle(quizDTO.getTitle());

        // If moduleId is being changed, update the module reference
        if (!quiz.getModule().getId().equals(quizDTO.getModuleId())) {
            Module newModule = moduleRepository.findById(quizDTO.getModuleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
            quiz.setModule(newModule);
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        return ResponseEntity.ok(mapToQuizDTO(updatedQuiz));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        if (!quizRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }

        quizRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
    private QuizDTO mapToQuizDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());

        if (quiz.getModule() != null) {
            dto.setModuleId(quiz.getModule().getId());
            dto.setModuleTitle(quiz.getModule().getTitle());
        }

        // We don't map questions here to avoid circular references
        // They will be loaded separately when needed

        return dto;
    }

    private List<QuizDTO> mapToQuizDTOs(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::mapToQuizDTO)
                .collect(Collectors.toList());
    }
}
