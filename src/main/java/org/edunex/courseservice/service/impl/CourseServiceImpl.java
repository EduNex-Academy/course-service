package org.edunex.courseservice.service.impl;

import org.edunex.courseservice.dto.CourseDTO;
import org.edunex.courseservice.dto.ModuleDTO;
import org.edunex.courseservice.model.Course;
import org.edunex.courseservice.model.CourseStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

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

    public List<CourseDTO> getAllCourses(String userId, CourseStatus status) {
        List<Course> courses;

        if (userId == null) {
            // Return courses filtered by status
            courses = courseRepository.findByStatus(status);
        } else {
            // Check if this user is an instructor with courses
            List<Course> instructorCourses = courseRepository.findByInstructorId(userId);
            if (instructorCourses != null && !instructorCourses.isEmpty()) {
                // If the user is an instructor, they can see all their courses
                // If status is specified, filter by status
                if (status != null) {
                    courses = courseRepository.findByInstructorIdAndStatus(userId, status);
                } else {
                    // If no status is specified, show all their instructor courses
                    courses = instructorCourses;
                }
            } else {
                // For a regular user, show their enrolled courses with the specified status
                courses = courseRepository.findEnrolledCoursesByUserIdAndStatus(userId, status);
            }
        }

        return mapToCourseDTOs(courses, userId);
    }
    
    // Keep the old method for backward compatibility
    public List<CourseDTO> getAllCourses(String userId) {
        return getAllCourses(userId, CourseStatus.PUBLISHED);
    }

    public CourseDTO getCourseById(Long id, String userId, boolean includeModules) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
                
        // If no specific user and course is not published, return 403
        if (userId == null && course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course not available");
        }
        
        return mapToCourseDTO(course, userId, includeModules);
    }
    
    public CourseDTO getCourseById(Long id, String userId, boolean includeModules, CourseStatus requiredStatus) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
                
        // If requiredStatus is provided, check that the course matches
        if (requiredStatus != null && course.getStatus() != requiredStatus) {
            // Only instructors can view their own draft courses
            boolean isInstructor = userId != null && course.getInstructorId() != null && 
                                  course.getInstructorId().equals(userId);
            if (!isInstructor) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Course not available with required status: " + requiredStatus);
            }
        }
        
        return mapToCourseDTO(course, userId, includeModules);
    }

    public List<CourseDTO> getCoursesByInstructorId(String instructorId, String userId) {
        // By default, only return published courses
        return getCoursesByInstructorId(instructorId, userId, CourseStatus.PUBLISHED);
    }
    
    public List<CourseDTO> getCoursesByInstructorId(String instructorId, String userId, CourseStatus status) {
        List<Course> courses;
        
        // Check if the requesting user is the instructor
        boolean isOwnCourses = userId != null && userId.equals(instructorId);
        
        if (isOwnCourses) {
            // For instructors viewing their own courses, show all courses regardless of status
            // If a specific status is requested, still apply that filter
            if (status != null) {
                courses = courseRepository.findByInstructorIdAndStatus(instructorId, status);
            } else {
                // If no status specified, return all courses for the instructor
                courses = courseRepository.findByInstructorId(instructorId);
            }
        } else {
            // For other users viewing an instructor's courses, only show courses with the requested status
            courses = courseRepository.findByInstructorIdAndStatus(instructorId, status);
        }
        
        return mapToCourseDTOs(courses, userId);
    }

    public List<CourseDTO> getCoursesByCategory(String category, String userId) {
        // By default, only return published courses
        return getCoursesByCategory(category, userId, CourseStatus.PUBLISHED);
    }
    
    public List<CourseDTO> getCoursesByCategory(String category, String userId, CourseStatus status) {
        // Need to filter by category and status
        List<Course> courses = courseRepository.findByCategoryAndStatus(category, status);
        return mapToCourseDTOs(courses, userId);
    }

    public List<CourseDTO> getEnrolledCourses(String userId) {
        // By default, only return published courses
        return getEnrolledCourses(userId, CourseStatus.PUBLISHED);
    }
    
    public List<CourseDTO> getEnrolledCourses(String userId, CourseStatus status) {
        List<Course> courses = courseRepository.findEnrolledCoursesByUserIdAndStatus(userId, status);
        return mapToCourseDTOs(courses, userId);
    }

    public List<CourseDTO> searchCourses(String query, String userId) {
        // By default, only return published courses when searching
        List<Course> courses = courseRepository.searchCoursesByStatus(query, CourseStatus.PUBLISHED);
        return mapToCourseDTOs(courses, userId);
    }
    
    public List<CourseDTO> searchCourses(String query, String userId, CourseStatus status) {
        List<Course> courses = courseRepository.searchCoursesByStatus(query, status);
        return mapToCourseDTOs(courses, userId);
    }

    public CourseDTO createCourse(CourseDTO courseDTO) {
        logger.debug("createCourse called with title={}, instructorId={}", courseDTO.getTitle(), courseDTO.getInstructorId());
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
        logger.info("Created course id={} title={}", savedCourse.getId(), savedCourse.getTitle());
        return mapToCourseDTO(savedCourse, null, false);
    }

    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        logger.debug("updateCourse called for id={} with title={}", id, courseDTO.getTitle());
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
        logger.info("Updated course id={}", updatedCourse.getId());
        return mapToCourseDTO(updatedCourse, null, false);
    }

    public void deleteCourse(Long id) {
        logger.debug("deleteCourse called for id={}", id);
        if (!courseRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existing course id={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
        courseRepository.deleteById(id);
        logger.info("Deleted course id={}", id);
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
        logger.debug("uploadCourseThumbnail called for courseId={}, originalFilename={}, size={}", id, file.getOriginalFilename(), file.getSize());
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
        logger.info("Uploaded thumbnail for courseId={} objectKey={}", id, objectKey);
        
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
        logger.debug("publishCourse called for id={} by userId={}", id, userId);
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
        logger.info("Published course id={} by instructorId={}", id, userId);
        
        // Return updated course
        return mapToCourseDTO(updatedCourse, userId, false);
    }
}
