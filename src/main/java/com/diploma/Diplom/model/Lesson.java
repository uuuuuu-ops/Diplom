package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "lessons")
public class Lesson {

    @Id
    private String id;

    private String courseId;

    private String title;
    private String description;

    private int orderIndex;
    private int duration; 

    private String videoUrl;
    private String videoFileName;

    private String lectureText;
    private String lecturePdfUrl;
    private String lecturePdfFileName;

    private boolean published;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String videoPublicId;
    private String lecturePdfPublicId;
}