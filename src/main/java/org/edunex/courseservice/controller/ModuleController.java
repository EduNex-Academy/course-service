package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.enums.ModuleType;
import org.edunex.courseservice.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        List<ModuleDTO> moduleDTOs = moduleService.getAllModules();
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModuleById(
            @PathVariable Long id,
            @RequestParam(required = false) String userId) {
        ModuleDTO moduleDTO = moduleService.getModuleById(id, userId);
        return ResponseEntity.ok(moduleDTO);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByCourseId(
            @PathVariable Long courseId,
            @RequestParam(required = false) String userId) {
        List<ModuleDTO> moduleDTOs = moduleService.getModulesByCourseId(courseId, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ModuleDTO>> getModulesByType(
            @PathVariable ModuleType type,
            @RequestParam(required = false) String userId) {
        List<ModuleDTO> moduleDTOs = moduleService.getModulesByType(type, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/course/{courseId}/available")
    public ResponseEntity<List<ModuleDTO>> getAvailableModulesByCourseIdAndCoins(
            @PathVariable Long courseId,
            @RequestParam int userCoins,
            @RequestParam(required = false) String userId) {
        List<ModuleDTO> moduleDTOs = moduleService.getAvailableModulesByCourseIdAndCoins(courseId, userCoins, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @PostMapping
    public ResponseEntity<ModuleDTO> createModule(@RequestBody ModuleDTO moduleDTO) {
        ModuleDTO createdModule = moduleService.createModule(moduleDTO);
        return new ResponseEntity<>(createdModule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleDTO> updateModule(@PathVariable Long id, @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO updatedModule = moduleService.updateModule(id, moduleDTO);
        return ResponseEntity.ok(updatedModule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<Void> reorderModule(@PathVariable Long id, @RequestParam int newOrder) {
        moduleService.reorderModule(id, newOrder);
        return ResponseEntity.ok().build();
    }
}
