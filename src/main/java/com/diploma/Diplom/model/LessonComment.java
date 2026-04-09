package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A Q&A comment or reply on a lesson")  
@Data
@Document(collection = "lesson_comments")
public class LessonComment {

 
    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;

    @Schema(description = "ID of the lesson this comment belongs to")
    private String lessonId;

    @Schema(description = "ID of the course this comment belongs to")
    private String courseId;

    @Schema(description = "ID of the user who wrote the comment")
    private String authorId;

    @Schema(description = "The text content of the comment or reply")
    private String content;

    @Schema(description = "null for a top-level comment; comment ID for a reply")
    private String parentId;
 
    @Schema(description = "True when a teacher marks this reply as the accepted answer")
    private boolean markedAsAnswer;

    @Schema(description = "Timestamp when the comment or reply was created", example = "2023-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the comment or reply was last updated", example = "2023-01-01T00:00:00")
    private LocalDateTime updatedAt;
}