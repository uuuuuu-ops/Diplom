package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
@Data
@Document(collection = "enrollments")
public class Enrollment {

    @Id
    private String id;

    private String userId;

    private String courseId;

    private LocalDateTime enrolledAt;
}
