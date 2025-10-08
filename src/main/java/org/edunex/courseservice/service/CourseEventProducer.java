package org.edunex.courseservice.service;

import lombok.RequiredArgsConstructor;
import org.edunex.courseservice.event.CourseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(CourseEventProducer.class);
    private static final String TOPIC = "course-events-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(CourseEvent event) {
        logger.info("Sending course event: {}", event);
        kafkaTemplate.send(TOPIC, event);
    }
}
