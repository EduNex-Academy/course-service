package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.ProgressDTO;
import org.edunex.courseservice.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    @GetMapping
    public ResponseEntity<List<ProgressDTO>> getAllProgress() {
        List<ProgressDTO> progressDTOs = progressService.getAllProgress();
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgressDTO> getProgressById(@PathVariable Long id) {
        ProgressDTO progressDTO = progressService.getProgressById(id);
        return ResponseEntity.ok(progressDTO);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ProgressDTO>> getProgressByUserId(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<ProgressDTO> progressDTOs = progressService.getProgressByUserId(userId);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ProgressDTO>> getProgressByModuleId(@PathVariable Long moduleId) {
        List<ProgressDTO> progressDTOs = progressService.getProgressByModuleId(moduleId);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/user/module/{moduleId}")
    public ResponseEntity<ProgressDTO> getProgressByUserIdAndModuleId(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        ProgressDTO progressDTO = progressService.getProgressByUserIdAndModuleId(userId, moduleId);
        return ResponseEntity.ok(progressDTO);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ProgressDTO>> getProgressByUserIdAndCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<ProgressDTO> progressDTOs = progressService.getProgressByUserIdAndCourseId(userId, courseId);
        return ResponseEntity.ok(progressDTOs);
    }

    @GetMapping("/course/{courseId}/stats")
    public ResponseEntity<Map<String, Object>> getCourseProgressStats(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Map<String, Object> stats = progressService.getCourseProgressStats(userId, courseId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/module/{moduleId}/complete")
    public ResponseEntity<ProgressDTO> markModuleAsCompleted(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        ProgressDTO progressDTO = progressService.markModuleAsCompleted(userId, moduleId, jwt);
        return new ResponseEntity<>(progressDTO, HttpStatus.CREATED);
    }

    @PostMapping("/module/{moduleId}/reset")
    public ResponseEntity<ProgressDTO> resetModuleProgress(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        ProgressDTO progressDTO = progressService.resetModuleProgress(userId, moduleId);
        return ResponseEntity.ok(progressDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgress(@PathVariable Long id) {
        progressService.deleteProgress(id);
        return ResponseEntity.noContent().build();
    }
}
