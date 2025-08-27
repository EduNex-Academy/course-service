package org.edunex.courseservice.service;

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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgressRepository progressRepository;

    public List<ModuleDTO> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        return mapToModuleDTOs(modules, null);
    }

    public ModuleDTO getModuleById(Long id, String userId) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        return mapToModuleDTO(module, userId);
    }

    public List<ModuleDTO> getModulesByCourseId(Long courseId, String userId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrder(courseId);
        return mapToModuleDTOs(modules, userId);
    }

    public List<ModuleDTO> getModulesByType(ModuleType type, String userId) {
        List<Module> modules = moduleRepository.findByType(type);
        return mapToModuleDTOs(modules, userId);
    }

    public List<ModuleDTO> getAvailableModulesByCourseIdAndCoins(Long courseId, int userCoins, String userId) {
        List<Module> modules = moduleRepository.findByCourseIdAndCoinsRequiredLessThanEqual(courseId, userCoins);
        return mapToModuleDTOs(modules, userId);
    }

    public ModuleDTO createModule(ModuleDTO moduleDTO) {
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
        return mapToModuleDTO(savedModule, null);
    }

    public ModuleDTO updateModule(Long id, ModuleDTO moduleDTO) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        module.setTitle(moduleDTO.getTitle());
        module.setType(moduleDTO.getType());
        module.setCoinsRequired(moduleDTO.getCoinsRequired());
        module.setContentUrl(moduleDTO.getContentUrl());
        module.setModuleOrder(moduleDTO.getModuleOrder());

        if (!module.getCourse().getId().equals(moduleDTO.getCourseId())) {
            Course newCourse = courseRepository.findById(moduleDTO.getCourseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
            module.setCourse(newCourse);
        }

        Module updatedModule = moduleRepository.save(module);
        return mapToModuleDTO(updatedModule, null);
    }

    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        moduleRepository.deleteById(id);
    }

    public void reorderModule(Long id, int newOrder) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        module.setModuleOrder(newOrder);
        moduleRepository.save(module);
    }

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
            // Handle potential lazy loading issues by checking if the session is still open
            try {
                dto.setCourseName(module.getCourse().getTitle());
            } catch (Exception e) {
                // If there's an issue accessing the course title, set a default or fetch it separately
                dto.setCourseName("Course #" + module.getCourse().getId());
            }
        }

        if (module.getQuiz() != null) {
            dto.setQuizId(module.getQuiz().getId());
        }

        if (userId != null) {
            Optional<Progress> progress = progressRepository.findByUserIdAndModuleId(userId, module.getId());
            dto.setCompleted(progress.isPresent() && progress.get().isCompleted());
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
