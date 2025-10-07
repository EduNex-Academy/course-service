package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.FileDTO;
import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.enums.ModuleType;
import org.edunex.courseservice.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        ModuleDTO moduleDTO = moduleService.getModuleById(id, userId);
        return ResponseEntity.ok(moduleDTO);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByCourseId(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<ModuleDTO> moduleDTOs = moduleService.getModulesByCourseId(courseId, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ModuleDTO>> getModulesByType(
            @PathVariable ModuleType type,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
        List<ModuleDTO> moduleDTOs = moduleService.getModulesByType(type, userId);
        return ResponseEntity.ok(moduleDTOs);
    }

    @GetMapping("/course/{courseId}/available")
    public ResponseEntity<List<ModuleDTO>> getAvailableModulesByCourseIdAndCoins(
            @PathVariable Long courseId,
            @RequestParam int userCoins,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt != null ? jwt.getSubject() : null;
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
    
    /**
     * Upload content (video or PDF) for a module
     */
    @PostMapping("/{id}/content")
    public ResponseEntity<FileDTO> uploadModuleContent(
            @PathVariable Long id, 
            @RequestParam("file") MultipartFile file) {
        FileDTO fileDTO = moduleService.uploadModuleContent(id, file);
        return new ResponseEntity<>(fileDTO, HttpStatus.CREATED);
    }
    
    /**
     * Download content for a module
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> downloadModuleContent(@PathVariable Long id) {
        return moduleService.downloadModuleContent(id);
    }
    
    /**
     * Delete content from a module
     */
    @DeleteMapping("/{id}/content")
    public ResponseEntity<Void> deleteModuleContent(@PathVariable Long id) {
        moduleService.deleteModuleContent(id);
        return ResponseEntity.noContent().build();
    }
}
