package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Schema(description = "Request containing the unique identifier for a pending subscription and the plan code for which to create a subscription")
@Data
public class SavePendingSubscriptionRequest {

    @NotBlank(message = "Subscription ID is required")
    @Schema(description = "The unique identifier for the pending subscription")
    private String subscriptionId;

    @NotBlank(message = "Plan code is required")
    @Schema(description = "The code of the plan for which to create a subscription")
    private String planCode;

}