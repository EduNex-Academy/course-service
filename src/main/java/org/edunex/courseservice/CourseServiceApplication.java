package org.edunex.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("org.edunex.courseservice.model")
public class CourseServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(CourseServiceApplication.class, args);
    }

}
