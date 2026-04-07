package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Request to confirm a PayPal subscription")
@Data
public class ConfirmPaypalSubscriptionRequest {
    @Schema(description = "The ID of the subscription to confirm")
    private String subscriptionId;
}