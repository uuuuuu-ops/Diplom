package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
@Schema(description = "Response containing information about the created PayPal order, including the order ID and approval URL")
@Data
@AllArgsConstructor
public class CreatePaypalOrderResponse {
   
    @Schema(description = "The unique identifier for the created PayPal order")
    private String orderId;

    @Schema(description = "The URL to approve the created PayPal order")
    private String approvalUrl;
}