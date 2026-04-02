package com.diploma.Diplom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

import java.time.LocalDateTime;
@Data
@Document(collection = "teacher_applications")
public class TeacherApplication {

    @Id
    private String id;

    private String userId;
    private String fullName;
    private String email;
    private String specialization;
    private int yearsOfExperience;

    private String resumeText;
    private String resumeFileName;
    private String resumeFileUrl;

    private String status;
    private String reviewComment;
    private Integer score;
    private LocalDateTime createdAt;
    
    private String aiSummary;
    private String aiStrengths;
    private String aiWeaknesses;
    private String aiRecommendation;

    private String resumePublicId;
}