package org.edunex.courseservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseEmailEvent {
    private String to;
    private String courseName;
    private String studentName;
    private String notificationType; // ENROLLMENT, COMPLETION, REMINDER
}
