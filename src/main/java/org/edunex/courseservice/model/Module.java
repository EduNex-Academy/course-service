package org.edunex.courseservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.edunex.courseservice.model.enums.ModuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleType type;

    //Coins required to do this module
    @Column(nullable = false)
    private int coinsRequired;

    // Stores the S3 object key for video/pdf content
    private String contentUrl;

    // To maintain the order of modules in a course
    private int moduleOrder;

    // A module belongs to one course
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore // Prevents infinite loops in JSON serialization
    private Course course;

    // A module can optionally have one quiz
    @OneToOne(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Quiz quiz;
}
