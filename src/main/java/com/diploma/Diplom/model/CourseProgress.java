package com.diploma.Diplom.model;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A student's progress record for one course")
@Data
@Document(collection = "course_progress")
public class CourseProgress {
 
    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;
    @Schema(description = "ID of the student this progress record belongs to")
    private String userId;
    @Schema(description = "ID of the course this progress record belongs to")
    private String courseId;

    @Schema(description = "IDs of lessons the student has completed")
    private Set<String> completedLessonIds;
 
    @Schema(description = "IDs of quizzes the student has passed")
    private Set<String> passedQuizIds;
 
    @Schema(description = "Overall completion percentage 0–100", example = "65")
    private int progressPercent;
 
    @Schema(description = "True when all lessons completed and all quizzes passed")
    private boolean completed;
    @Schema(description = "Timestamp when the progress record was last updated", example = "2023-01-01T00:00:00")
    private LocalDateTime lastUpdatedAt;
    @Schema(description = "Timestamp when the course was completed", example = "2023-01-01T00:00:00")
    private LocalDateTime completedAt;
}