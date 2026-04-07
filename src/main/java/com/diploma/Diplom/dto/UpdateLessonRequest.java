package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "A request to update lesson details, including title, description, order index, duration, lecture text, and publication status")
@Data
public class UpdateLessonRequest {
    @Schema(description = "The title of the lesson, which is required for the update")
    private String title;
    @Schema(description = "The description of the lesson, which is required for the update")
    private String description;
    @Schema(description = "The order index of the lesson, which is required for the update")
    private Integer orderIndex;
    @Schema(description = "The duration of the lesson, which is required for the update")
    private Integer duration;
    @Schema(description = "The lecture text of the lesson, which is required for the update")
    private String lectureText;
    @Schema(description = "The publication status of the lesson, which is required for the update")
    private Boolean published;
    @Schema(description = "The quiz requirement of the lesson, which is required for the update")
    private Boolean quizRequired;
}