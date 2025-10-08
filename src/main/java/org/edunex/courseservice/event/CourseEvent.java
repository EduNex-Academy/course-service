package org.edunex.courseservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CourseEvent {
    private String userId;
    private String courseId;
    private String courseName;
    private String eventType;
    private String message;
    private String notificationType; //in notification service it is enum but here it is string
}

