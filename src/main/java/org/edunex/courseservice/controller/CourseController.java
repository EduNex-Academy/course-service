package org.edunex.courseservice.controller;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.Enrollment;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ProgressRepository progressRepository;

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses(@RequestParam(required = false) String userId) {
        List<Course> courses = courseRepository.findAll();
        List<CourseDTO> courseDTOs = mapToCourseDTOs(courses, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(
            @PathVariable Long id,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "false") boolean includeModules) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        CourseDTO courseDTO = mapToCourseDTO(course, userId, includeModules);
        return ResponseEntity.ok(courseDTO);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructorId(
            @PathVariable String instructorId,
            @RequestParam(required = false) String userId) {
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        List<CourseDTO> courseDTOs = mapToCourseDTOs(courses, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CourseDTO>> getCoursesByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String userId) {
        List<Course> courses = courseRepository.findByCategory(category);
        List<CourseDTO> courseDTOs = mapToCourseDTOs(courses, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/enrolled")
    public ResponseEntity<List<CourseDTO>> getEnrolledCourses(@RequestParam String userId) {
        List<Course> courses = courseRepository.findEnrolledCoursesByUserId(userId);
        List<CourseDTO> courseDTOs = mapToCourseDTOs(courses, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(
            @RequestParam String query,
            @RequestParam(required = false) String userId) {
        List<Course> courses = courseRepository.searchCourses(query);
        List<CourseDTO> courseDTOs = mapToCourseDTOs(courses, userId);
        return ResponseEntity.ok(courseDTOs);
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO courseDTO) {
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructorId(courseDTO.getInstructorId());
        course.setCategory(courseDTO.getCategory());
        course.setCreatedAt(LocalDateTime.now());

        Course savedCourse = courseRepository.save(course);
        return new ResponseEntity<>(mapToCourseDTO(savedCourse, null, false), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCategory(courseDTO.getCategory());

        // Instructor can't be changed unless by admin - would need additional checks here

        Course updatedCourse = courseRepository.save(course);
        return ResponseEntity.ok(mapToCourseDTO(updatedCourse, null, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }

        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper methods to map between entity and DTO
    private CourseDTO mapToCourseDTO(Course course, String userId, boolean includeModules) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setInstructorId(course.getInstructorId());
        dto.setCategory(course.getCategory());
        dto.setCreatedAt(course.getCreatedAt());

        // Set module count
        dto.setModuleCount(course.getModules() != null ? course.getModules().size() : 0);

        // Set enrollment count
        dto.setEnrollmentCount(course.getEnrollments() != null ? course.getEnrollments().size() : 0);

        // Check if user is enrolled
        if (userId != null) {
            dto.setUserEnrolled(enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId()));

            // Calculate completion percentage if user is enrolled
            if (dto.isUserEnrolled()) {
                Long completedModules = progressRepository.countCompletedModulesByCourseAndUser(userId, course.getId());
                Long totalModules = (long) dto.getModuleCount();
                dto.setCompletionPercentage(totalModules > 0 ? (double) completedModules / totalModules * 100 : 0);
            }
        }

        // Include modules if requested
        if (includeModules) {
            List<ModuleDTO> moduleDTOs = moduleRepository.findByCourseIdOrderByModuleOrder(course.getId())
                    .stream()
                    .map(module -> {
                        ModuleDTO moduleDTO = new ModuleDTO();
                        moduleDTO.setId(module.getId());
                        moduleDTO.setTitle(module.getTitle());
                        moduleDTO.setType(module.getType());
                        moduleDTO.setCoinsRequired(module.getCoinsRequired());
                        moduleDTO.setContentUrl(module.getContentUrl());
                        moduleDTO.setModuleOrder(module.getModuleOrder());
                        moduleDTO.setCourseId(course.getId());
                        moduleDTO.setCourseName(course.getTitle());

                        if (module.getQuiz() != null) {
                            moduleDTO.setQuizId(module.getQuiz().getId());
                        }

                        return moduleDTO;
                    })
                    .collect(Collectors.toList());

            dto.setModules(moduleDTOs);
        }

        return dto;
    }

    private List<CourseDTO> mapToCourseDTOs(List<Course> courses, String userId) {
        return courses.stream()
                .map(course -> mapToCourseDTO(course, userId, false))
                .collect(Collectors.toList());
    }
}
