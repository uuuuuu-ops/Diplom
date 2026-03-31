package com.diploma.Diplom.service;

import com.diploma.Diplom.config.PaypalProperties;
import com.diploma.Diplom.dto.CreatePaypalOrderResponse;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.PaymentRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaypalService {

    private final PaypalProperties paypalProperties;
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaypalService(PaypalProperties paypalProperties,
                         PaymentRepository paymentRepository,
                         CourseRepository courseRepository,
                         UserRepository userRepository,
                         EnrollmentService enrollmentService) {
        this.paypalProperties = paypalProperties;
        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentService = enrollmentService;
    }

    public String getCurrentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    public CreatePaypalOrderResponse createCourseOrder(String courseId) {
        String userId = getCurrentUserId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.isFree()) {
            throw new RuntimeException("Free course does not require payment");
        }

        if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Course price is invalid");
        }

        if (enrollmentService.hasAccess(userId, courseId)) {
            throw new RuntimeException("You already have access to this course");
        }

        String accessToken = getAccessToken();

        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", course.getCurrency());
        amount.put("value", course.getPrice().toPlainString());

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("reference_id", course.getId());
        purchaseUnit.put("amount", amount);

        Map<String, Object> body = new HashMap<>();
        body.put("intent", "CAPTURE");
        body.put("purchase_units", List.of(purchaseUnit));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                paypalProperties.getBaseUrl() + "/v2/checkout/orders",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Empty PayPal response");
        }

        String orderId = (String) responseBody.get("id");
        String approvalUrl = extractApprovalUrl(responseBody);

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setCourseId(courseId);
        payment.setType(PaymentType.COURSE_PURCHASE);
        payment.setStatus(PaymentStatus.CREATED);
        payment.setAmount(course.getPrice());
        payment.setCurrency(course.getCurrency());
        payment.setProvider("PAYPAL");
        payment.setPaypalOrderId(orderId);
        payment.setApprovalUrl(approvalUrl);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return new CreatePaypalOrderResponse(orderId, approvalUrl);
    }

    public Payment captureOrder(String orderId) {
        String userId = getCurrentUserId();

        Payment payment = paymentRepository.findByPaypalOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            return payment;
        }

        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                paypalProperties.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Empty PayPal capture response");
        }

        String status = (String) body.get("status");
        if (!"COMPLETED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        }

        String captureId = extractCaptureId(body);

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setPaypalCaptureId(captureId);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        enrollmentService.activatePurchasedEnrollment(
                savedPayment.getUserId(),
                savedPayment.getCourseId(),
                savedPayment.getId()
        );

        return savedPayment;
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(paypalProperties.getClientId(), paypalProperties.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                paypalProperties.getBaseUrl() + "/v1/oauth2/token",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new RuntimeException("Failed to get PayPal access token");
        }

        return (String) responseBody.get("access_token");
    }

    private String extractApprovalUrl(Map<String, Object> responseBody) {
        Object linksObj = responseBody.get("links");
        if (!(linksObj instanceof List<?> links)) {
            return null;
        }

        for (Object linkObj : links) {
            if (linkObj instanceof Map<?, ?> linkMap) {
                Object rel = linkMap.get("rel");
                Object href = linkMap.get("href");
                if ("approve".equals(rel) && href != null) {
                    return href.toString();
                }
            }
        }
        return null;
    }

    private String extractCaptureId(Map<String, Object> responseBody) {
        Object purchaseUnitsObj = responseBody.get("purchase_units");
        if (!(purchaseUnitsObj instanceof List<?> purchaseUnits) || purchaseUnits.isEmpty()) {
            return null;
        }

        Object firstPurchaseUnit = purchaseUnits.get(0);
        if (!(firstPurchaseUnit instanceof Map<?, ?> purchaseUnitMap)) {
            return null;
        }

        Object paymentsObj = purchaseUnitMap.get("payments");
        if (!(paymentsObj instanceof Map<?, ?> paymentsMap)) {
            return null;
        }

        Object capturesObj = paymentsMap.get("captures");
        if (!(capturesObj instanceof List<?> captures) || captures.isEmpty()) {
            return null;
        }

        Object firstCapture = captures.get(0);
        if (!(firstCapture instanceof Map<?, ?> captureMap)) {
            return null;
        }

        Object id = captureMap.get("id");
        return id != null ? id.toString() : null;
    }
}