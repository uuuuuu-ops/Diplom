package com.diploma.Diplom.dto;

import lombok.Data;

@Data
public class UpdateLessonRequest {

    private String title;
    private String description;
    private Integer orderIndex;
    private Integer duration;
    private String lectureText;
    private Boolean published;
    private Boolean quizRequired;
}