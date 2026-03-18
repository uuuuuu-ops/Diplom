package com.diploma.Diplom.model;

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

    private String videoUrl;

    private int order;

    private int duration;
}