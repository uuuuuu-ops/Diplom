package com.diploma.Diplom.dto;

import lombok.Data;

@Data
public class UpdateCourseRequest {

    private String title;
    private String description;
    private String category;
    private String level;
    private Boolean published;
}