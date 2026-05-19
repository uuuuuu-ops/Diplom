package com.diploma.Diplom.service;

import com.diploma.Diplom.config.PaypalProperties;
import com.diploma.Diplom.dto.CreatePaypalOrderResponse;
import com.diploma.Diplom.exception.ConflictException;
import com.diploma.Diplom.exception.PaymentException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.exception.UnauthorizedException;
import com.diploma.Diplom.messaging.PaymentProducer;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.util.SecurityUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PaypalService {

    private final PaypalProperties paypalProperties;
    private final PaymentService paymentService;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    private final PaypalTokenRedisCache tokenCache;
    private final SecurityUtils securityUtils;
    private final PaymentProducer paymentProducer;

    private final RestTemplate restTemplate = new RestTemplate();

    public PaypalService(PaypalProperties paypalProperties,
                         PaymentService paymentService,
                         CourseRepository courseRepository,
                         EnrollmentService enrollmentService,
                         PaypalTokenRedisCache tokenCache,
                         SecurityUtils securityUtils,
                         PaymentProducer paymentProducer) {
        this.paypalProperties = paypalProperties;
        this.paymentService = paymentService;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
        this.tokenCache = tokenCache;
        this.securityUtils = securityUtils;
        this.paymentProducer = paymentProducer;
    }

    public CreatePaypalOrderResponse createCourseOrder(String courseId) {
        String userId = securityUtils.getCurrentUserId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        if (course.isFree()) {
            throw new PaymentException("Free course does not require payment");
        }

        if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Course price is invalid");
        }

        if (enrollmentService.hasAccess(userId, courseId)) {
            throw new ConflictException("You already have access to this course");
        }

        String accessToken = tokenCache.getAccessToken();

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

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paypalProperties.getBaseUrl() + "/v2/checkout/orders",
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new PaymentException("Empty response from PayPal");
        }

        String orderId = (String) responseBody.get("id");
        String approvalUrl = extractApprovalUrl(responseBody);

        paymentService.createCoursePayment(
                userId, courseId, orderId, approvalUrl,
                course.getPrice(), course.getCurrency()
        );

        return new CreatePaypalOrderResponse(orderId, approvalUrl);
    }

    public Payment captureOrder(String orderId) {
        String userId = securityUtils.getCurrentUserId();

        Payment payment = paymentService.getByOrderId(orderId);

        if (!payment.getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this payment");
        }

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            return payment;
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new PaymentException("Cannot capture a failed payment");
        }

        String accessToken = tokenCache.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paypalProperties.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture",
            HttpMethod.POST,
            new HttpEntity<>("{}", headers),
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new PaymentException("Empty capture response from PayPal");
        }

        String status = (String) responseBody.get("status");
        if (!"COMPLETED".equalsIgnoreCase(status)) {
            return paymentService.markAsFailed(orderId);
        }

        String captureId = extractCaptureId(responseBody);
        Payment captured = paymentService.markAsCaptured(orderId, captureId);

        // Async: enrollment + invoice email через RabbitMQ
        paymentProducer.sendPaymentCaptured(
                captured.getUserId(),
                captured.getCourseId(),
                captured.getId(),
                captured.getAmount(),
                captured.getCurrency()
        );

        return captured;
    }

    private String extractApprovalUrl(Map<String, Object> responseBody) {
        Object linksObj = responseBody.get("links");
        if (!(linksObj instanceof List<?> links)) return null;

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
        if (!(purchaseUnitsObj instanceof List<?> purchaseUnits) || purchaseUnits.isEmpty()) return null;
        if (!(purchaseUnits.get(0) instanceof Map<?, ?> purchaseUnitMap)) return null;

        Object paymentsObj = purchaseUnitMap.get("payments");
        if (!(paymentsObj instanceof Map<?, ?> paymentsMap)) return null;

        Object capturesObj = paymentsMap.get("captures");
        if (!(capturesObj instanceof List<?> captures) || captures.isEmpty()) return null;
        if (!(captures.get(0) instanceof Map<?, ?> captureMap)) return null;

        Object id = captureMap.get("id");
        return id != null ? id.toString() : null;
    }
}
