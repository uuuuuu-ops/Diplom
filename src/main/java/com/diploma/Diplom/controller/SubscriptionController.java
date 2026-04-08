package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.ConfirmPaypalSubscriptionRequest;
import com.diploma.Diplom.dto.SavePendingSubscriptionRequest;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.service.PaypalSubscriptionService;
import com.diploma.Diplom.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscriptions/paypal")
@Tag(name = "Enrollments & Payments", description = "Enroll in courses and manage payments")
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
        summary = "Get the PayPal subscription plan ID",
        description = """
            Returns the configured PayPal plan ID that the frontend passes to
            the PayPal JS SDK when rendering the subscription button.
            """,
        responses = @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(example = "{\"planId\": \"P-58N535868G1027601NHFZDXQ\"}")))
    )
    @GetMapping("/plan")
    public Map<String, String> getPlan() {
        return Map.of("planId", paypalSubscriptionService.getPlanId());
    }

    @Operation(
        summary = "Confirm an active PayPal subscription",
        description = """
            Call this after PayPal redirects back to your app with a `subscription_id`.
            Verifies the subscription with PayPal and activates it in the database.

            **Body:** `{ "subscriptionId": "I-XXXXXXXXXXXX" }`
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Subscription activated",
                content = @Content(schema = @Schema(implementation = Subscription.class))),
            @ApiResponse(responseCode = "400", description = "Subscription not active on PayPal",
                content = @Content)
        }
    )
    @PostMapping("/confirm")
    public Subscription confirm(@RequestBody ConfirmPaypalSubscriptionRequest request) {
        return paypalSubscriptionService.confirmSubscription(request.getSubscriptionId());
    }

    @Operation(
        summary = "Save a pending subscription",
        description = """
            Saves the subscription in PENDING state before the user completes PayPal approval.
            Call this immediately after the PayPal button creates the subscription,
            so the record exists even if the user closes the window mid-flow.

            **Body:** `{ "subscriptionId": "I-XXXXXXXXXXXX" }`
            """,
        responses = @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = Subscription.class)))
    )
    @PostMapping("/save-pending")
    public Subscription savePending(@RequestBody SavePendingSubscriptionRequest request) {
        String planCode = (request.getPlanCode() != null && !request.getPlanCode().isBlank())
                ? request.getPlanCode()
                : "PRO";
        return paypalSubscriptionService.savePendingSubscription(
                request.getSubscriptionId(), planCode);
    }

    @Operation(
        summary = "Get my subscriptions",
        description = "Returns all subscription records for the authenticated user.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Subscription.class))))
    )
    @GetMapping("/my")
    public List<Subscription> mySubscriptions() {
        return subscriptionService.getMySubscriptions();
    }
}