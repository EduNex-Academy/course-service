package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.model.enums.ModuleType;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        List<ModuleDTO> moduleDTOs = mapToModuleDTOs(modules, null);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModuleById(
            @PathVariable Long id,
            @RequestParam(required = false) String userId) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        return ResponseEntity.ok(mapToModuleDTO(module, userId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByCourseId(
            @PathVariable Long courseId,
            @RequestParam(required = false) String userId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrder(courseId);
        List<ModuleDTO> moduleDTOs = mapToModuleDTOs(modules, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ModuleDTO>> getModulesByType(
            @PathVariable ModuleType type,
            @RequestParam(required = false) String userId) {
        List<Module> modules = moduleRepository.findByType(type);
        List<ModuleDTO> moduleDTOs = mapToModuleDTOs(modules, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/course/{courseId}/available")
    public ResponseEntity<List<ModuleDTO>> getAvailableModulesByCourseIdAndCoins(
            @PathVariable Long courseId,
            @RequestParam int userCoins,
            @RequestParam(required = false) String userId) {
        List<Module> modules = moduleRepository.findByCourseIdAndCoinsRequiredLessThanEqual(courseId, userCoins);
        List<ModuleDTO> moduleDTOs = mapToModuleDTOs(modules, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @PostMapping
    public ResponseEntity<ModuleDTO> createModule(@RequestBody ModuleDTO moduleDTO) {
        Course course = courseRepository.findById(moduleDTO.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Module module = new Module();
        module.setTitle(moduleDTO.getTitle());
        module.setType(moduleDTO.getType());
        module.setCoinsRequired(moduleDTO.getCoinsRequired());
        module.setContentUrl(moduleDTO.getContentUrl());
        module.setModuleOrder(moduleDTO.getModuleOrder());
        module.setCourse(course);

        Module savedModule = moduleRepository.save(module);
        return new ResponseEntity<>(mapToModuleDTO(savedModule, null), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Long id, @RequestBody ModuleDTO moduleDTO) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        module.setTitle(moduleDTO.getTitle());
        module.setType(moduleDTO.getType());
        module.setCoinsRequired(moduleDTO.getCoinsRequired());
        module.setContentUrl(moduleDTO.getContentUrl());
        module.setModuleOrder(moduleDTO.getModuleOrder());

        // If courseId is being changed, update the course reference
        if (!module.getCourse().getId().equals(moduleDTO.getCourseId())) {
            Course newCourse = courseRepository.findById(moduleDTO.getCourseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
            module.setCourse(newCourse);
        }

        Module updatedModule = moduleRepository.save(module);
        return ResponseEntity.ok(mapToModuleDTO(updatedModule, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }

        moduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<Void> reorderModule(@PathVariable Long id, @RequestParam int newOrder) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        module.setModuleOrder(newOrder);
        moduleRepository.save(module);

        return ResponseEntity.ok().build();
    }

    // Helper methods to map between entity and DTO
    private ModuleDTO mapToModuleDTO(Module module, String userId) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setType(module.getType());
        dto.setCoinsRequired(module.getCoinsRequired());
        dto.setContentUrl(module.getContentUrl());
        dto.setModuleOrder(module.getModuleOrder());

        if (module.getCourse() != null) {
            dto.setCourseId(module.getCourse().getId());
            dto.setCourseName(module.getCourse().getTitle());
        }

        if (module.getQuiz() != null) {
            dto.setQuizId(module.getQuiz().getId());
        }

        // Set completion status if userId is provided
        if (userId != null) {
            Optional<Progress> progress = progressRepository.findByUserIdAndModuleId(userId, module.getId());
            dto.setCompleted(progress.isPresent() && progress.get().isCompleted());

            // Calculate progress percentage for the module (implementation depends on your business logic)
            // This is a placeholder - you'd need to implement actual progress calculation
            dto.setProgressPercentage(dto.isCompleted() ? 100.0 : 0.0);
        }

        return dto;
    }

    private List<ModuleDTO> mapToModuleDTOs(List<Module> modules, String userId) {
        return modules.stream()
                .map(module -> mapToModuleDTO(module, userId))
                .collect(Collectors.toList());
    }
}
