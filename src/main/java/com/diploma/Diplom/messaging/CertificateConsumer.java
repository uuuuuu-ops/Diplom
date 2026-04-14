package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateConsumer {

    private final CertificateService certificateService;
    private final CertificateRepository certificateRepository;

    @RabbitListener(queues = RabbitMQConfig.CERTIFICATE_QUEUE)
    public void handleCertificate(CertificateMessage message) {
        try {
            boolean exists = certificateRepository
                    .findByUserIdAndCourseId(message.getUserId(), message.getCourseId())
                    .isPresent();

            if (exists) {
                log.info("Certificate already exists for userId={}, courseId={} — skipping",
                        message.getUserId(), message.getCourseId());
                return;
            }

            certificateService.issueCertificate(message.getUserId(), message.getCourseId());
            log.info("Certificate issued for userId={}, courseId={}",
                    message.getUserId(), message.getCourseId());
        } catch (Exception e) {
            log.error("Failed to generate certificate for userId={}, courseId={}: {}",
                    message.getUserId(), message.getCourseId(), e.getMessage());
            throw e; // уйдёт в DLQ
        }
    }
}