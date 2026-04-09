package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A single lesson inside a course") 
@Data
@Document(collection = "lessons")
public class Lesson {

    @Id
    @Schema(description = "MongoDB ObjectId")
    private String id;

    @Schema(description = "ID of the course this lesson belongs to")
    private String courseId;

    @Schema(description = "Title of the lesson", example = "Introduction to Java")
    private String title;

    @Schema(description = "Description of the lesson", example = "Learn the basics of Java programming")
    private String description;

    @Schema(description = "Position in the course (0 = first lesson)", example = "0")
    private int orderIndex;

    @Schema(description = "Estimated duration in minutes", example = "15")
    private int duration;

    @Schema(description = "URL of the video for this lesson FROM Cloudinary", example = "https://res.cloudinary.com/demo/video/upload/v1234567890/sample.mp4")
    private String videoUrl;

    @Schema(description = "Name of the video file", example = "sample.mp4")
    private String videoFileName;

    @Schema(description = "Text content for the lecture", example = "Welcome to the introduction to Java!")
    private String lectureText;

    @Schema(description = "URL of the lecture PDF", example = "https://res.cloudinary.com/demo/pdf/upload/v1234567890/lecture.pdf")
    private String lecturePdfUrl;

    @Schema(description = "Name of the lecture PDF file", example = "lecture.pdf")
    private String lecturePdfFileName;

    @Schema(description = "If true, the lesson is published and available to students", example = "true")
    private boolean published;

    @Schema(description = "If true, student must pass this lesson's quiz before marking complete")
    private boolean quizRequired;

    @Schema(description = "Timestamp when the lesson was created", example = "2023-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the lesson was last updated", example = "2023-01-01T00:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Public ID of the video file in Cloudinary", example = "sample_mp4")
    private String videoPublicId;
    
    @Schema(description = "Public ID of the lecture PDF file in Cloudinary", example = "lecture_pdf")
    private String lecturePdfPublicId;
}