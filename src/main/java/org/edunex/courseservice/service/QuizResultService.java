package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizResultDTO;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizResult;
import org.edunex.courseservice.repository.QuizRepository;
import org.edunex.courseservice.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuizRepository quizRepository;

    public List<QuizResultDTO> getAllQuizResults() {
        List<QuizResult> results = quizResultRepository.findAll();
        return mapToQuizResultDTOs(results);
    }

    public QuizResultDTO getQuizResultById(Long id) {
        QuizResult result = quizResultRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz result not found"));
        return mapToQuizResultDTO(result);
    }

    public List<QuizResultDTO> getQuizResultsByUserId(String userId) {
        List<QuizResult> results = quizResultRepository.findByUserId(userId);
        return mapToQuizResultDTOs(results);
    }

    public List<QuizResultDTO> getQuizResultsByQuizId(Long quizId) {
        List<QuizResult> results = quizResultRepository.findByQuizId(quizId);
        return mapToQuizResultDTOs(results);
    }

    public List<QuizResultDTO> getQuizResultsByUserIdAndQuizId(String userId, Long quizId) {
        List<QuizResult> results = quizResultRepository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(userId, quizId);
        return mapToQuizResultDTOs(results);
    }

    public QuizResultDTO getBestQuizResultForUser(String userId, Long quizId) {
        QuizResult result = quizResultRepository.findTopByUserIdAndQuizIdOrderByScoreDesc(userId, quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz result found"));
        return mapToQuizResultDTO(result);
    }

    public QuizResultDTO createQuizResult(QuizResultDTO quizResultDTO) {
        // Validate the quiz exists
        Quiz quiz = quizRepository.findById(quizResultDTO.getQuizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        // The userId will now come from the JWT token in the controller
        QuizResult quizResult = new QuizResult();
        quizResult.setUserId(quizResultDTO.getUserId());
        quizResult.setQuiz(quiz);
        quizResult.setScore(quizResultDTO.getScore());
        quizResult.setSubmittedAt(LocalDateTime.now());

        QuizResult savedResult = quizResultRepository.save(quizResult);
        return mapToQuizResultDTO(savedResult);
    }

    public void deleteQuizResult(Long id) {
        if (!quizResultRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz result not found");
        }
        quizResultRepository.deleteById(id);
    }

    private QuizResultDTO mapToQuizResultDTO(QuizResult quizResult) {
        QuizResultDTO dto = new QuizResultDTO();
        dto.setId(quizResult.getId());
        dto.setUserId(quizResult.getUserId());
        dto.setQuizId(quizResult.getQuiz().getId());
        dto.setQuizTitle(quizResult.getQuiz().getTitle());
        dto.setScore(quizResult.getScore());
        dto.setSubmittedAt(quizResult.getSubmittedAt());

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
