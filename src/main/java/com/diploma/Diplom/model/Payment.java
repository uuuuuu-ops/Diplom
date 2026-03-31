package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String userId;
    private String courseId;

    private PaymentType type;
    private PaymentStatus status;

    private BigDecimal amount;
    private String currency;

    private String provider; 
    private String paypalOrderId;
    private String paypalCaptureId;

    private String approvalUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}