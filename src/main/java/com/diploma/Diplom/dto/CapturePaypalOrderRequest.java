package com.diploma.Diplom.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
@Schema(description = "Request containing user credentials for capturing a PayPal order")
@Data
public class CapturePaypalOrderRequest {
    @NotBlank(message = "Order ID is required")
    @Schema(description = "The ID of the PayPal order to be captured")
    private String orderId;
}