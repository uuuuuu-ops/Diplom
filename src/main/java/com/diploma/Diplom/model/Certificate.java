package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "certificates")
public class Certificate {

    @Id
    private String id;

    private String userId;
    private String courseId;

    private String studentName;
    private String courseTitle;
    private String instructorName;

    private String certificateNumber;
    private String verificationCode;

    private LocalDateTime issuedAt;
    private LocalDateTime regeneratedAt;

    private String templateVersion;
    private String pdfUrl;

    private boolean active;
}