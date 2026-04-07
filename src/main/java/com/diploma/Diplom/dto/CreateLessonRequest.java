package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "Request to create a new lesson")
@Data
public class CreateLessonRequest {
    @Schema(description = "The title of the lesson")
    private String title;
    @Schema(description = "The description of the lesson")
    private String description;
    @Schema(description = "The index of the lesson in the course (starting from 0)")
    private int orderIndex;
    @Schema(description = "The duration of the lesson in minutes")
    private int duration;
    @Schema(description = "The text of the lesson lecture")
    private String lectureText;
    @Schema(description = "Whether a quiz is required for the lesson")
    private boolean quizRequired = false;
}