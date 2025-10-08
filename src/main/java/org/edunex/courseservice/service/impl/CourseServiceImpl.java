package org.edunex.courseservice.service.impl;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.CourseStatus;
import org.edunex.courseservice.event.CourseEvent;
import org.edunex.courseservice.service.CourseEventProducer;
import org.edunex.courseservice.repository.CourseRepository;
import org.edunex.courseservice.repository.EnrollmentRepository;
import org.edunex.courseservice.repository.ModuleRepository;
import org.edunex.courseservice.repository.ProgressRepository;
import org.edunex.courseservice.service.CourseService;
import org.edunex.courseservice.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {


    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ProgressRepository progressRepository;
    
    @Autowired
    private S3Service s3Service;

    @Autowired
    private CourseEventProducer courseEventProducer;

    public List<CourseDTO> getAllCourses(String userId) {
        List<Course> courses;

        if (userId == null) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findEnrolledCoursesByUserId(userId);
        }

        return mapToCourseDTOs(courses, userId);
    }

    public CourseDTO getCourseById(Long id, String userId, boolean includeModules) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return mapToCourseDTO(course, userId, includeModules);
    }

    public List<CourseDTO> getCoursesByInstructorId(String instructorId, String userId) {
        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        return mapToCourseDTOs(courses, userId);
    }

    public List<CourseDTO> getCoursesByCategory(String category, String userId) {
        List<Course> courses = courseRepository.findByCategory(category);
        return mapToCourseDTOs(courses, userId);
    }

    public List<CourseDTO> getEnrolledCourses(String userId) {
        List<Course> courses = courseRepository.findEnrolledCoursesByUserId(userId);
        return mapToCourseDTOs(courses, userId);
    }

    public List<CourseDTO> searchCourses(String query, String userId) {
        List<Course> courses = courseRepository.searchCourses(query);
        return mapToCourseDTOs(courses, userId);
    }

    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setInstructorId(courseDTO.getInstructorId());
        course.setCategory(courseDTO.getCategory());
        course.setCreatedAt(LocalDateTime.now());
        
        // Explicitly set status from DTO or default to DRAFT
        if (courseDTO.getStatus() != null) {
            course.setStatus(courseDTO.getStatus());
        } else {
            course.setStatus(CourseStatus.DRAFT);
        }

        Course savedCourse = courseRepository.save(course);
        // Send event after course creation
        CourseEvent event = new CourseEvent(
            course.getInstructorId(),
            savedCourse.getId() != null ? savedCourse.getId().toString() : null,
            savedCourse.getTitle(),
            "COURSE_CREATED",
            "A new course titled '" + savedCourse.getTitle() + "' has been published.",
            "PUSH"
        );
        courseEventProducer.sendEvent(event);



        return mapToCourseDTO(savedCourse, null, false);
    }

    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCategory(courseDTO.getCategory());

        // Update status if provided in the DTO
        if (courseDTO.getStatus() != null) {
            course.setStatus(courseDTO.getStatus());
        }

        // Instructor can't be changed unless by admin - would need additional checks here

        Course updatedCourse = courseRepository.save(course);
        // Send event after course update
        CourseEvent event = new CourseEvent(
            course.getInstructorId(),
            updatedCourse.getId() != null ? updatedCourse.getId().toString() : null,
            updatedCourse.getTitle(),
            "COURSE_UPDATED",
            "Course content for '" + updatedCourse.getTitle() + "' was updated.",
            "PUSH"
        );
        courseEventProducer.sendEvent(event);
        return mapToCourseDTO(updatedCourse, null, false);


    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        courseRepository.deleteById(id);
    }

    public CourseDTO mapToCourseDTO(Course course, String userId, boolean includeModules) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setInstructorId(course.getInstructorId());
        dto.setCategory(course.getCategory());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        dto.setStatus(course.getStatus());

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

    public List<CourseDTO> mapToCourseDTOs(List<Course> courses, String userId) {
        return courses.stream()
                .map(course -> mapToCourseDTO(course, userId, false))
                .collect(Collectors.toList());
    }
    
    /**
     * Upload a thumbnail image for a course
     * 
     * @param id The course ID
     * @param file The thumbnail image file
     * @return The updated course DTO with thumbnail URL
     */
    @Override
    public CourseDTO uploadCourseThumbnail(Long id, MultipartFile file) {
        // Find the course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        
        // Delete old thumbnail if it exists
        if (course.getThumbnailObjectKey() != null && !course.getThumbnailObjectKey().isEmpty()) {
            s3Service.deleteFile(course.getThumbnailObjectKey());
        }
        
        // Upload the new thumbnail
        String objectKey = s3Service.uploadCourseThumbnail(file, id);
        
        // Generate CloudFront URL
        String thumbnailUrl = s3Service.getCloudFrontUrl(objectKey);
        
        // Update course with new thumbnail details
        course.setThumbnailObjectKey(objectKey);
        course.setThumbnailUrl(thumbnailUrl);
        Course updatedCourse = courseRepository.save(course);
        
        // Return updated course
        return mapToCourseDTO(updatedCourse, null, false);
    }
    
    /**
     * Publish a course, changing its status from DRAFT to PUBLISHED
     * 
     * @param id The course ID
     * @param userId The ID of the user trying to publish the course
     * @return The updated course DTO with PUBLISHED status
     */
    @Override
    public CourseDTO publishCourse(Long id, String userId) {
        // Find the course
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        
        // Validate that the user is the instructor of the course
        if (!course.getInstructorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Only the instructor of the course can publish it");
        }
        
        // Check if the course is already published
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Course is already published");
        }
        
        // Update the course status to PUBLISHED
        course.setStatus(CourseStatus.PUBLISHED);
        Course updatedCourse = courseRepository.save(course);
        // Send event after course publish
        CourseEvent event = new CourseEvent(
            userId,
            updatedCourse.getId() != null ? updatedCourse.getId().toString() : null,
            updatedCourse.getTitle(),
            "COURSE_CREATED",
            "A new course titled '" + updatedCourse.getTitle() + "' has been published.",
            "PUSH"
        );
        courseEventProducer.sendEvent(event);
        return mapToCourseDTO(updatedCourse, userId, false);
    }
}
