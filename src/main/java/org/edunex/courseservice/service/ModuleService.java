package org.edunex.courseservice.service;

import org.edunex.courseservice.dto.FileDTO;
import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Module;
import org.edunex.courseservice.model.Progress;
import org.edunex.courseservice.model.enums.ModuleType;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    @Autowired
    private S3Service s3Service;

    private static final Logger logger = LoggerFactory.getLogger(ModuleService.class);

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
        logger.debug("mapToModuleDTO called for moduleId={}", module.getId());
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setType(module.getType());
        dto.setCoinsRequired(module.getCoinsRequired());
        dto.setContentUrl(module.getContentUrl());
        
        // Set CloudFront URL if content URL exists
        if (module.getContentUrl() != null && !module.getContentUrl().isEmpty()) {
            dto.setContentCloudFrontUrl(s3Service.getCloudFrontUrl(module.getContentUrl()));
            logger.debug("Set content CloudFront URL for moduleId={} url={}", module.getId(), dto.getContentCloudFrontUrl());
        }
        
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

        logger.debug("mapToModuleDTO completed for moduleId={} completed={}", module.getId(), dto.isCompleted());

        return dto;
    }

    private List<ModuleDTO> mapToModuleDTOs(List<Module> modules, String userId) {
        return modules.stream()
                .map(module -> mapToModuleDTO(module, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Upload content file for a module
     * @param moduleId The ID of the module
     * @param file The file to upload
     * @return FileDTO with metadata about the uploaded file
     */
    public FileDTO uploadModuleContent(Long moduleId, MultipartFile file) {
        logger.debug("uploadModuleContent called for moduleId={} filename={} size={}", moduleId, file.getOriginalFilename(), file.getSize());
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        
        // Check if the file is acceptable (video or PDF)
        String contentType = file.getContentType();
        if (contentType == null || 
                !(contentType.startsWith("video/") || contentType.equals("application/pdf"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Invalid file type. Only videos and PDFs are allowed.");
        }
        
        // If the module already has content, delete the old one
        if (module.getContentUrl() != null && !module.getContentUrl().isEmpty()) {
            s3Service.deleteFile(module.getContentUrl());
        }
        
        // Upload the new file
        String objectKey = s3Service.uploadFile(file, moduleId);
        
        // Update the module with the new content URL
        module.setContentUrl(objectKey);
        moduleRepository.save(module);
        
        // Set the module type based on the content type
        if (contentType.startsWith("video/")) {
            module.setType(ModuleType.VIDEO);
        } else if (contentType.equals("application/pdf")) {
            module.setType(ModuleType.PDF);
        }
        moduleRepository.save(module);

    logger.info("Uploaded module content moduleId={} objectKey={} contentType={}", moduleId, objectKey, file.getContentType());
        
        // Get CloudFront URL for this object
        String cloudFrontUrl = s3Service.getCloudFrontUrl(objectKey);
        
        // Create and return the file DTO with CloudFront URL
        return new FileDTO(
                moduleId,
                file.getOriginalFilename(),
                contentType,
                objectKey,
                cloudFrontUrl,
                file.getSize()
        );
    }
    
    /**
     * Download content for a module
     * @param moduleId The ID of the module
     * @return ResponseEntity with the file content
     */
    public ResponseEntity<InputStreamResource> downloadModuleContent(Long moduleId) {
        logger.debug("downloadModuleContent called for moduleId={}", moduleId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        
        if (module.getContentUrl() == null || module.getContentUrl().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module has no content");
        }
        
        // Check if the file exists in S3
        if (!s3Service.doesFileExist(module.getContentUrl())) {
            logger.warn("File not found in S3 for moduleId={} key={}", moduleId, module.getContentUrl());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in storage");
        }
        
        logger.info("Downloading module content for moduleId={} key={}", moduleId, module.getContentUrl());
        return s3Service.downloadFile(module.getContentUrl());
    }
    
    /**
     * Delete content from a module
     * @param moduleId The ID of the module
     */
    public void deleteModuleContent(Long moduleId) {
        logger.debug("deleteModuleContent called for moduleId={}", moduleId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        
        if (module.getContentUrl() == null || module.getContentUrl().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module has no content to delete");
        }
        
        // Delete the file from S3
        s3Service.deleteFile(module.getContentUrl());

        logger.info("Deleted module content for moduleId={} key={}", moduleId, module.getContentUrl());
        
        // Update the module
        module.setContentUrl(null);
        moduleRepository.save(module);
    }
}
