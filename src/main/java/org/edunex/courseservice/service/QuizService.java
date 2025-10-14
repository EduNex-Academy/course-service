package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.QuizAnswerDTO;
import org.edunex.courseservice.dto.QuizDTO;
import org.edunex.courseservice.dto.QuizQuestionDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Quiz;
import org.edunex.courseservice.model.QuizAnswer;
import org.edunex.courseservice.model.QuizQuestion;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ModuleRepository moduleRepository;


    public List<QuizDTO> getAllQuizzes() {
        List<Quiz> quizzes = quizRepository.findAll();
        return mapToQuizDTOs(quizzes);
    }

    public QuizDTO getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        return mapToQuizDTO(quiz);
    }

    public List<QuizDTO> getQuizzesByModuleId(Long moduleId) {
        List<Quiz> quizzes = quizRepository.findByModuleId(moduleId);
        return mapToQuizDTOs(quizzes);
    }

    public QuizDTO createQuiz(QuizDTO quizDTO) {
        Module module = moduleRepository.findById(quizDTO.getModuleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setModule(module);

        // Initialize an empty list if questions are null
        List<QuizQuestion> questions = new ArrayList<>();

        if (quizDTO.getQuestions() != null) {
            questions = quizDTO.getQuestions().stream()
                    .map(this::mapToQuizQuestionEntity)
                    .collect(Collectors.toList());

            // Set the quiz reference for each question
            questions.forEach(question -> question.setQuiz(quiz));
        }

        quiz.setQuestions(questions);

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizDTO(savedQuiz);
    }

    private QuizQuestion mapToQuizQuestionEntity(QuizQuestionDTO dto) {
        QuizQuestion question = new QuizQuestion();
        question.setId(dto.getId());
        question.setQuestionText(dto.getQuestionText());

        // Handle null or empty answers list
        List<QuizAnswer> answers = new ArrayList<>();
        
        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
            // 1. First, create the list of QuizAnswer entities
            answers = dto.getAnswers().stream()
                    .map(this::mapToQuizAnswerEntity)
                    .collect(Collectors.toList());

            // 2. Set the back-reference on each answer to its parent question
            answers.forEach(answer -> answer.setQuestion(question));
        }

        // 3. Now, set the complete list on the question object
        question.setAnswers(answers);

        return question;
    }

    private QuizAnswer mapToQuizAnswerEntity(QuizAnswerDTO dto) {
        if (dto.getAnswerText() == null || dto.getAnswerText().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer text cannot be null or empty");
        }
        
        QuizAnswer answer = new QuizAnswer();
        answer.setId(dto.getId());
        answer.setAnswerText(dto.getAnswerText());
        answer.setCorrect(dto.isCorrect()); // This will default to false for primitive boolean
        return answer;
    }

    public QuizDTO updateQuiz(Long id, QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

        quiz.setTitle(quizDTO.getTitle());

        if (!quiz.getModule().getId().equals(quizDTO.getModuleId())) {
            Module newModule = moduleRepository.findById(quizDTO.getModuleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
            quiz.setModule(newModule);
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        return mapToQuizDTO(updatedQuiz);
    }

    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
        }
        quizRepository.deleteById(id);
    }

    private QuizDTO mapToQuizDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());

        if (quiz.getModule() != null) {
            dto.setModuleId(quiz.getModule().getId());
            dto.setModuleTitle(quiz.getModule().getTitle());
        }

        return dto;
    }

    private List<QuizDTO> mapToQuizDTOs(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::mapToQuizDTO)
                .collect(Collectors.toList());
    }
}
