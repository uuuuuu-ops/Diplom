package com.diploma.Diplom.dto;

import java.util.List;

import lombok.Data;

@Data
public class PayPalSubscriptionResponse {
    private String id;
    private List<PayPalLink> links;
}
