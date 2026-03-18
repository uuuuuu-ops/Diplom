package com.diploma.Diplom.dto;

import lombok.Data;

@Data
public class CreateLessonRequest {

    private String title;
    private String description;
    private int orderIndex;
    private int duration;
    private String lectureText;
}