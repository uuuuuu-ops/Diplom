package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
@Schema(description = "Response containing information about the created PayPal subscription, including the subscription ID")
@Data
@AllArgsConstructor
public class CreatePaypalSubscriptionResponse {
   
    @Schema(description = "The unique identifier for the created PayPal subscription")
    private String subscriptionId;
}