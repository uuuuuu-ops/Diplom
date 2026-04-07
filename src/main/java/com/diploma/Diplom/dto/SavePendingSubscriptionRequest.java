package com.diploma.Diplom.dto;

public class SavePendingSubscriptionRequest {

    private String subscriptionId;
    private String planCode;

    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
}