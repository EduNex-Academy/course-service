package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.ProgressDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private ModuleRepository moduleRepository;

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

    public ProgressDTO markModuleAsCompleted(String userId, Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElse(new Progress());

        progress.setUserId(userId);
        progress.setModule(module);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        Progress savedProgress = progressRepository.save(progress);
        return mapToProgressDTO(savedProgress);
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
}
