package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "enrollments")
public class Enrollment {

    @Id
    private String id;

    private String userId;
    private String courseId;

    private AccessType accessType;
    private EnrollmentStatus status;

    private String paymentId;
    private String subscriptionId;

    private LocalDateTime enrolledAt;
    private LocalDateTime expiresAt;
}