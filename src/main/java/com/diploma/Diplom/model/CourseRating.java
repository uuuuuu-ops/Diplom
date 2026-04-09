package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A student's rating for a course")  
@Data
@Document(collection = "course_ratings")
public class CourseRating {
    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;

    @Schema(description = "ID of the student who submitted the rating")
    private String userId;

    @Schema(description = "ID of the course being rated")
    private String courseId;

    @Schema(description = "Star rating from 1 to 5", example = "4")
    private int rating;
 
    @Schema(description = "Optional written review", example = "Great course, very clear explanations!")
    private String review;

    @Schema(description = "Timestamp when the rating was created", example = "2023-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the rating was last updated", example = "2023-01-01T00:00:00")
    private LocalDateTime updatedAt;
}