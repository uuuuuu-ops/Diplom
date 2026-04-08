package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "A course offered on the platform")
@Data
@Document(collection = "courses")
public class Course {

    @Id
    @Schema(description = "MongoDB ObjectId")
    private String id;

    @Schema(description = "Course title displayed to students", example = "Java for Beginners")
    private String title;

    @Schema(description = "Full course description (supports markdown)", example = "Learn Java from scratch...")
    private String description;

    @Schema(description = "MongoDB ID of the teacher who owns this course")
    private String teacherId;
 
    @Schema(description = "Category tag", example = "Programming")
    private String category;
 
    @Schema(description = "Difficulty level", example = "beginner", allowableValues = {"beginner","intermediate","advanced"})
    private String level;
 
    @Schema(description = "Cloudinary URL of the thumbnail image")
    private String thumbnail;
    @Schema(description = "Whether the course is visible to students")
    private boolean published;
 
    @Schema(description = "If true, students can enroll without paying")
    private boolean free;
 
    @Schema(description = "Price in USD. Ignored when free=true", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Currency code", example = "USD")
    private String currency;


    private List<String> accessibleBySubscriptions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String thumbnailPublicId;

    @Schema(description = "Average rating 1.0–5.0, recalculated on every new rating", example = "4.3")
    private double avgRating;
 
    @Schema(description = "Total number of ratings submitted", example = "47")
    private int ratingCount;
}