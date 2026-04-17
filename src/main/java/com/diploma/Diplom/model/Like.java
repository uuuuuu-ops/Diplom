package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;


@Schema(description = "Likes for courses")
@Data
@Document(collection = "likes")
@CompoundIndex(name = "user_course_idx", def = "{'userId': 1, 'courseId': 1}", unique = true)
public class Like {

    @Schema(description = "Id for Likes")
    @Id
    private String id;
    @Schema(description = "UserId for likes")
    private String userId;
    @Schema(description = "courseId for likes")
    private String courseId;
    @Schema(description = "Created time")
    private Instant createdAt = Instant.now();
}