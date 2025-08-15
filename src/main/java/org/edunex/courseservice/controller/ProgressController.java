package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.ProgressDTO;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @GetMapping
    public ResponseEntity<List<ProgressDTO>> getAllProgress() {
        List<Progress> progressList = progressRepository.findAll();
        List<ProgressDTO> progressDTOs = mapToProgressDTOs(progressList);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgressDTO> getProgressById(@PathVariable Long id) {
        Progress progress = progressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progress record not found"));

        return ResponseEntity.ok(mapToProgressDTO(progress));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgressDTO>> getProgressByUserId(@PathVariable String userId) {
        List<Progress> progressList = progressRepository.findByUserId(userId);
        List<ProgressDTO> progressDTOs = mapToProgressDTOs(progressList);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ProgressDTO>> getProgressByModuleId(@PathVariable Long moduleId) {
        List<Progress> progressList = progressRepository.findByModuleId(moduleId);
        List<ProgressDTO> progressDTOs = mapToProgressDTOs(progressList);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/user/{userId}/module/{moduleId}")
    public ResponseEntity<ProgressDTO> getProgressByUserIdAndModuleId(
            @PathVariable String userId,
            @PathVariable Long moduleId) {
        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Progress record not found for this user and module"));

        return ResponseEntity.ok(mapToProgressDTO(progress));
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<List<ProgressDTO>> getProgressByUserIdAndCourseId(
            @PathVariable String userId,
            @PathVariable Long courseId) {
        List<Progress> progressList = progressRepository.findByCourseIdAndUserId(userId, courseId);
        List<ProgressDTO> progressDTOs = mapToProgressDTOs(progressList);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/user/{userId}/course/{courseId}/stats")
    public ResponseEntity<Map<String, Object>> getCourseProgressStats(
            @PathVariable String userId,
            @PathVariable Long courseId) {

        Long completedModules = progressRepository.countCompletedModulesByCourseAndUser(userId, courseId);
        Long totalModules = progressRepository.countModulesByCourse(courseId);

        double completionPercentage = totalModules > 0 ?
                (double) completedModules / totalModules * 100 : 0;

        Map<String, Object> stats = Map.of(
                "userId", userId,
                "courseId", courseId,
                "completedModules", completedModules,
                "totalModules", totalModules,
                "completionPercentage", completionPercentage
        );

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/user/{userId}/module/{moduleId}/complete")
    public ResponseEntity<ProgressDTO> markModuleAsCompleted(
            @PathVariable String userId,
            @PathVariable Long moduleId) {

        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElse(new Progress());

        progress.setUserId(userId);
        progress.setModule(module);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        Progress savedProgress = progressRepository.save(progress);
        return new ResponseEntity<>(mapToProgressDTO(savedProgress), HttpStatus.CREATED);
    }

    @PostMapping("/user/{userId}/module/{moduleId}/reset")
    public ResponseEntity<ProgressDTO> resetModuleProgress(
            @PathVariable String userId,
            @PathVariable Long moduleId) {

        Progress progress = progressRepository.findByUserIdAndModuleId(userId, moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Progress record not found for this user and module"));

        progress.setCompleted(false);
        progress.setCompletedAt(null);

        Progress savedProgress = progressRepository.save(progress);
        return ResponseEntity.ok(mapToProgressDTO(savedProgress));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgress(@PathVariable Long id) {
        if (!progressRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Progress record not found");
        }

        progressRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
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
