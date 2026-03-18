package com.diploma.Diplom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@Document(collection = "progress")
public class Progress {

    @Id
    private String id;

    private String userId;
    private String courseId;
    private List<String> completedLessons = new ArrayList<>();
    private int progressPercent;

    
}