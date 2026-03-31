package com.diploma.Diplom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaypalOrderResponse {
    private String orderId;
    private String approvalUrl;
}