package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "A payment for a course on the platform")
@Data
@Document(collection = "payments")
public class Payment {
    @Schema(description = "A payment for a course on the platform")
    @Id
    private String id;

    @Schema(description = "The user who made the payment")
    private String userId;

    @Schema(description = "The course for which the payment was made")
    private String courseId;

    @Schema(description = "The type of the payment, e.g., 'course_purchase', 'subscription', etc.")
    private PaymentType type;

    @Schema(description = "The status of the payment, e.g., 'pending', 'completed', 'failed'")
    private PaymentStatus status;

    @Schema(description = "The amount of the payment")
    private BigDecimal amount;

    @Schema(description = "The currency of the payment, e.g., 'USD', 'EUR'")
    private String currency;

    @Schema(description = "The provider of the payment, e.g., 'PayPal', 'Stripe', etc.")
    private String provider;

    @Schema(description = "The PayPal order ID for the payment, if applicable")
    private String paypalOrderId;

    @Schema(description = "The PayPal capture ID for the payment, if applicable")
    private String paypalCaptureId;

    @Schema(description = "The approval URL for the payment, if applicable")
    private String approvalUrl;

    @Schema(description = "The date and time when the payment was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "The date and time when the payment was last updated")
    private LocalDateTime updatedAt;
}