package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Schema(description = "Request containing the plan code for creating a new subscription")
@Data
public class CreateSubscriptionRequest {
    
    @NotBlank(message = "Plan code is required")
    @Schema(description = "The code of the plan for which to create a subscription")
    private String planCode; 
}