package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String title;

    private String description;

    private String teacherId;

    private String category;

    private String thumbnail;

    private boolean published;

    private LocalDateTime createdAt;
}    

