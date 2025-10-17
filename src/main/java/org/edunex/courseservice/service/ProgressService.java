package org.edunex.courseservice.service;

import lombok.RequiredArgsConstructor;
import org.edunex.courseservice.dto.ProgressDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.edunex.courseservice.event.CourseEmailEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;

    private final ModuleRepository moduleRepository;

    private final CourseEventProducer courseEventProducer;

    public List<ProgressDTO> getAllProgress() {
        List<Progress> progressList = progressRepository.findAll();
        return mapToProgressDTOs(progressList);
    }

    public ProgressDTO getProgressById(Long id) {
        Progress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progress record not found"));
        return mapToProgressDTO(progress);
    }

    public List<ProgressDTO> getProgressByUserId(String userId) {
        List<Progress> progressList = progressRepository.findByUserId(userId);
        return mapToProgressDTOs(progressList);
    }

    public List<ProgressDTO> getProgressByModuleId(Long moduleId) {
        List<Progress> progressList = progressRepository.findByModuleId(moduleId);
        return mapToProgressDTOs(progressList);
    }

    public ProgressDTO getProgressByUserIdAndModuleId(String userId, Long moduleId) {
        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Progress record not found for this user and module"));
        return mapToProgressDTO(progress);
    }

    public List<ProgressDTO> getProgressByUserIdAndCourseId(String userId, Long courseId) {
        List<Progress> progressList = progressRepository.findByCourseIdAndUserId(userId, courseId);
        return mapToProgressDTOs(progressList);
    }

    public Map<String, Object> getCourseProgressStats(String userId, Long courseId) {
        Long completedModules = progressRepository.countCompletedModulesByCourseAndUser(userId, courseId);
        Long totalModules = progressRepository.countModulesByCourse(courseId);

        double completionPercentage = totalModules > 0 ?
                (double) completedModules / totalModules * 100 : 0;

        return Map.of(
                "userId", userId,
                "courseId", courseId,
                "completedModules", completedModules,
                "totalModules", totalModules,
                "completionPercentage", completionPercentage
        );
    }

    // Modified to accept Jwt so we can extract email/name and send completion email when course is finished
    public ProgressDTO markModuleAsCompleted(String userId, Long moduleId, Jwt jwt) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElse(new Progress());

        progress.setUserId(userId);
        progress.setModule(module);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        Progress savedProgress = progressRepository.save(progress);

        // After marking module completed, check if the user has completed all modules for the course
        if (module.getCourse() != null) {
            Long courseId = module.getCourse().getId();
            Long completedModules = progressRepository.countCompletedModulesByCourseAndUser(userId, courseId);
            Long totalModules = progressRepository.countModulesByCourse(courseId);

            if (totalModules > 0 && completedModules.equals(totalModules)) {
                // User completed the course - send course completion email event
                String email = extractEmailFromJwt(jwt, userId);
                String studentName = extractNameFromJwt(jwt, userId);

                CourseEmailEvent emailEvent = new CourseEmailEvent(
                        email,
                        module.getCourse().getTitle(),
                        studentName,
                        "COURSE_COMPLETION"
                );

                courseEventProducer.sendEmailEvent(emailEvent);
            }
        }

        return mapToProgressDTO(savedProgress);
    }

    // Overload kept for compatibility (no JWT available)
    public ProgressDTO markModuleAsCompleted(String userId, Long moduleId) {
        return markModuleAsCompleted(userId, moduleId, null);
    }

    public ProgressDTO resetModuleProgress(String userId, Long moduleId) {
        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Progress record not found for this user and module"));

        progress.setCompleted(false);
        progress.setCompletedAt(null);

        Progress savedProgress = progressRepository.save(progress);
        return mapToProgressDTO(savedProgress);
    }

    public void deleteProgress(Long id) {
        if (!progressRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Progress record not found");
        }
        progressRepository.deleteById(id);
    }

    private ProgressDTO mapToProgressDTO(Progress progress) {
        ProgressDTO dto = new ProgressDTO();
        dto.setId(progress.getId());
        dto.setUserId(progress.getUserId());
        dto.setCompleted(progress.isCompleted());
        dto.setCompletedAt(progress.getCompletedAt());

        if (progress.getModule() != null) {
            Module module = progress.getModule();
            dto.setModuleId(module.getId());
            dto.setModuleTitle(module.getTitle());
            dto.setModuleType(module.getType());

            if (module.getCourse() != null) {
                dto.setCourseId(module.getCourse().getId());
                dto.setCourseTitle(module.getCourse().getTitle());
            }
        }

        return dto;
    }

    private List<ProgressDTO> mapToProgressDTOs(List<Progress> progressList) {
        return progressList.stream()
                .map(this::mapToProgressDTO)
                .collect(Collectors.toList());
    }

    // Helper methods copied from EnrollmentService to extract email and name from Jwt
    private String extractEmailFromJwt(Jwt jwt, String userId) {
        if (jwt != null) {
            String email = jwt.getClaim("email");
            if (email != null && !email.isEmpty()) {
                return email;
            }
        }

        if (userId != null && userId.contains("@")) {
            return userId;
        }

        return userId + "@edunex.academy";
    }

    private String extractNameFromJwt(Jwt jwt, String userId) {
        if (jwt != null) {
            String name = jwt.getClaim("name");
            if (name == null || name.isEmpty()) {
                name = jwt.getClaim("given_name");
            }
            if (name == null || name.isEmpty()) {
                name = jwt.getClaim("preferred_username");
            }
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        if (userId != null && userId.contains("@")) {
            String localPart = userId.substring(0, userId.indexOf('@'));
            return localPart.replace(".", " ");
        }

        return userId;
    }
}
