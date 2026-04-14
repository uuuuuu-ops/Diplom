package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.ConfirmPaypalSubscriptionRequest;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.service.PaypalSubscriptionService;
import com.diploma.Diplom.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscriptions/paypal")
@Tag(name = "Enrollments & Payments", description = "Manage PayPal subscriptions for course enrollment")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {
    private final PaypalSubscriptionService paypalSubscriptionService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(PaypalSubscriptionService paypalSubscriptionService,
                                  SubscriptionService subscriptionService) {
        this.paypalSubscriptionService = paypalSubscriptionService;
        this.subscriptionService = subscriptionService;
    }


    

    @Operation(
            summary = "Get PayPal plan ID",
            description = "Returns PayPal plan ID configured in backend. Used for subscription creation.",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(example = "{\"planId\": \"P-58N535868G1027601NHFZDXQ\"}")
                    )
            )
    )
    @GetMapping("/plan")
    public Map<String, String> getPlan() {
        return Map.of("planId", paypalSubscriptionService.getPlanId());
    }

    @Operation(
            summary = "Create PayPal subscription",
            description = """
                    Creates a PayPal subscription and returns approval URL.
                    User must open this URL to approve payment in PayPal.

                    Flow:
                    1. Backend creates subscription in PayPal
                    2. Returns approvalUrl
                    3. User opens link and approves payment
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Approval URL generated",
                            content = @Content(
                                    schema = @Schema(example = "{\"approvalUrl\": \"https://www.sandbox.paypal.com/checkoutnow?token=XXX\"}")
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Failed to create subscription")
            }
    )
    @PostMapping("/create")
    public Map<String, String> createSubscription() {
        String approvalUrl = paypalSubscriptionService.createSubscriptionAndGetApprovalLink();
        return Map.of("approvalUrl", approvalUrl);
    }

    @Operation(
            summary = "Confirm PayPal subscription",
            description = """
                    Confirms subscription after PayPal approval.

                    Steps:
                    1. User approves subscription in PayPal
                    2. PayPal redirects back with subscriptionId
                    3. Backend verifies subscription status with PayPal API
                    4. If ACTIVE → subscription is activated in DB
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ConfirmPaypalSubscriptionRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Subscription activated",
                            content = @Content(schema = @Schema(implementation = Subscription.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Subscription not active in PayPal")
            }
    )
    @PostMapping("/confirm")
    public Subscription confirm(@RequestBody ConfirmPaypalSubscriptionRequest request) {
        return paypalSubscriptionService.confirmSubscription(request.getSubscriptionId());
    }

    @Operation(
            summary = "Get my subscriptions",
            description = "Returns all subscriptions for the currently authenticated user",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = Subscription.class))
            )
    )
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Subscription> mySubscriptions() {
        return subscriptionService.getMySubscriptions();
    }
}