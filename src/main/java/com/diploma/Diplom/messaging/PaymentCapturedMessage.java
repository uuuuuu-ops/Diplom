package com.diploma.Diplom.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCapturedMessage implements Serializable {
    private String userId;
    private String courseId;
    private String paymentId;
    private BigDecimal amount;
    private String currency;
}