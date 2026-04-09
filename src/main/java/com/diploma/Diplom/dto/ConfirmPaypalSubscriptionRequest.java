package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Request to confirm a PayPal subscription")
@Data
public class ConfirmPaypalSubscriptionRequest {
    
    @Schema(description = "The ID of the subscription to confirm")
    @NotBlank(message = "Subscription ID is required")
    private String subscriptionId;
}