package com.diploma.Diplom.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "course_progress")
public class CourseProgress {

    @Id
    private String id;

    private String userId;
    private String courseId;

    // completed lessons
    private Set<String> completedLessonIds = new HashSet<>();

    // passed quizzes
    private Set<String> passedQuizIds = new HashSet<>();

    private int progressPercent;
    private boolean completed;

    private LocalDateTime lastUpdatedAt;
    private LocalDateTime completedAt;
}