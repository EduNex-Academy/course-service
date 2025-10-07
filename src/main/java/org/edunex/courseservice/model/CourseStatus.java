package org.edunex.courseservice.model;

/**
 * Enum representing the status of a course.
 * DRAFT - Course is not yet published and only visible to the instructor.
 * PUBLISHED - Course is published and visible to students.
 */
public enum CourseStatus {
    DRAFT, 
    PUBLISHED
}