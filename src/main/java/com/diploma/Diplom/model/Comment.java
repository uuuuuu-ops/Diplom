package com.diploma.Diplom.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A comment or reply on a lesson or course")
@Data
@Document(collection = "lesson_comments")
public class Comment {

    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;

    @Schema(description = "ID of the lesson this comment belongs to (null for course-level comments)")
    @Indexed
    private String targetId;

    @Schema(description = "ID of the user who wrote the comment")
    private String authorId;

    private CommentTargetType targetType;

    @Schema(description = "Display name of the author (denormalized for performance)")
    private String authorName;

    @Schema(description = "The text content of the comment or reply")
    private String content;

    @Schema(description = "null for a top-level comment; parent comment ID for a reply")
    private String parentId;

    @Schema(description = "True when a teacher marks this reply as the accepted answer")
    private boolean markedAsAnswer;

    @Schema(description = "True if the comment has been edited after creation")
    private boolean edited = false;

    @Schema(description = "Timestamp when the comment was created")
    private Instant createdAt = Instant.now();

    @Schema(description = "Timestamp when the comment was last updated")
    private Instant updatedAt = Instant.now();
}