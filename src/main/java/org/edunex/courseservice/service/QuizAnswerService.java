package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizAnswerDTO;
import org.edunex.courseservice.model.QuizAnswer;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.QuizAnswerRepository;
import org.edunex.courseservice.repository.QuizQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizAnswerService {

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    public List<QuizAnswerDTO> getAllQuizAnswers() {
        List<QuizAnswer> answers = quizAnswerRepository.findAll();
        return mapToQuizAnswerDTOs(answers);
    }

    public QuizAnswerDTO getQuizAnswerById(Long id) {
        QuizAnswer answer = quizAnswerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz answer not found"));
        return mapToQuizAnswerDTO(answer);
    }

    public List<QuizAnswerDTO> getQuizAnswersByQuestionId(Long questionId) {
        List<QuizAnswer> answers = quizAnswerRepository.findByQuestionId(questionId);
        return mapToQuizAnswerDTOs(answers);
    }

    public List<QuizAnswerDTO> getCorrectQuizAnswersByQuestionId(Long questionId) {
        List<QuizAnswer> answers = quizAnswerRepository.findByQuestionIdAndCorrect(questionId, true);
        return mapToQuizAnswerDTOs(answers);
    }

    public QuizAnswerDTO createQuizAnswer(QuizAnswerDTO answerDTO) {
        QuizQuestion question = quizQuestionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));

        QuizAnswer answer = new QuizAnswer();
        answer.setAnswerText(answerDTO.getAnswerText());
        answer.setCorrect(answerDTO.isCorrect());
        answer.setQuestion(question);

        QuizAnswer savedAnswer = quizAnswerRepository.save(answer);
        return mapToQuizAnswerDTO(savedAnswer);
    }

    public QuizAnswerDTO updateQuizAnswer(Long id, QuizAnswerDTO answerDTO) {
        QuizAnswer answer = quizAnswerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz answer not found"));

        answer.setAnswerText(answerDTO.getAnswerText());
        answer.setCorrect(answerDTO.isCorrect());

        if (!answer.getQuestion().getId().equals(answerDTO.getQuestionId())) {
            QuizQuestion newQuestion = quizQuestionRepository.findById(answerDTO.getQuestionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz question not found"));
            answer.setQuestion(newQuestion);
        }

        QuizAnswer updatedAnswer = quizAnswerRepository.save(answer);
        return mapToQuizAnswerDTO(updatedAnswer);
    }

    public void deleteQuizAnswer(Long id) {
        if (!quizAnswerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz answer not found");
        }
        quizAnswerRepository.deleteById(id);
    }

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
