package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CapturePaypalOrderRequest;
import com.diploma.Diplom.dto.CreatePaypalOrderResponse;
import com.diploma.Diplom.model.Payment;
import com.diploma.Diplom.service.PaymentService;
import com.diploma.Diplom.service.PaypalService;
import com.diploma.Diplom.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments/paypal")
@Tag(name = "Enrollments & Payments", description = "Enroll in courses and manage payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaypalService paypalService;
    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    public PaymentController(PaypalService paypalService,
                             PaymentService paymentService,
                             SecurityUtils securityUtils) {
        this.paypalService = paypalService;
        this.paymentService = paymentService;
        this.securityUtils = securityUtils;
    }

    @Operation(
        summary = "Create a PayPal order for a course purchase",
        description = """
            Step 1 of the PayPal checkout flow.

            Returns an `orderId` and an `approveUrl`. Redirect the user to `approveUrl`
            so they can approve the payment in PayPal. After approval, PayPal redirects
            back to your frontend — then call **POST /payments/paypal/orders/capture**.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Order created",
                content = @Content(schema = @Schema(implementation = CreatePaypalOrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Already enrolled", content = @Content)
        }
    )
    @PostMapping("/orders/course/{courseId}")
    public CreatePaypalOrderResponse createCourseOrder(
            @Parameter(description = "Course to purchase") @PathVariable String courseId) {
        return paypalService.createCourseOrder(courseId);
    }

    @Operation(
        summary = "Capture a PayPal order after user approval",
        description = """
            Step 2 of the PayPal checkout flow. Call this after the user has approved
            the payment on PayPal's page.

            On success, an enrollment is automatically created for the student.

            **Body:** `{ "orderId": "PAYPAL_ORDER_ID" }`
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Payment captured and enrollment activated",
                content = @Content(schema = @Schema(implementation = Payment.class))),
            @ApiResponse(responseCode = "400", description = "PayPal order not approved or already failed",
                content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
        }
    )
    @PostMapping("/orders/capture")
    public Payment captureOrder(@RequestBody CapturePaypalOrderRequest request) {
        return paypalService.captureOrder(request.getOrderId());
    }

    @Operation(
        summary = "Get my payment history",
        description = "Returns all payments made by the authenticated user.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Payment.class))))
    )
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public List<Payment> getMyPayments() {
        return paymentService.getPaymentsByUser(securityUtils.getCurrentUserId());
    }
}