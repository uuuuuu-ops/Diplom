package com.diploma.Diplom.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.diploma.Diplom.exception.InternalServerException;
import com.diploma.Diplom.model.Certificate;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfCertificateService {

    private final TemplateEngine templateEngine;
    private final QrCodeService qrCodeService;

    @Value("${certificate.storage.path}")
    private String storagePath;

    @Value("${app.base-url}")
    private String baseUrl;

    public PdfCertificateService(TemplateEngine templateEngine, QrCodeService qrCodeService) {
        this.templateEngine = templateEngine;
        this.qrCodeService = qrCodeService;
    }

    public String generateCertificatePdf(Certificate certificate) {
        try {
            File folder = new File(storagePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String verificationUrl = baseUrl + "/api/certificates/verify/" + certificate.getVerificationCode();
            String qrBase64 = qrCodeService.generateQrCodeBase64(verificationUrl, 200, 200);

            Map<String, Object> variables = new HashMap<>();
            variables.put("studentName", certificate.getStudentName());
            variables.put("courseTitle", certificate.getCourseTitle());
            variables.put("instructorName", certificate.getInstructorName());
            variables.put("issuedAt", certificate.getIssuedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            variables.put("certificateNumber", certificate.getCertificateNumber());
            variables.put("verificationCode", certificate.getVerificationCode());
            variables.put("verificationUrl", verificationUrl);
            variables.put("qrCodeBase64", qrBase64);

            Context context = new Context();
            context.setVariables(variables);

            String html = templateEngine.process("certificate-template", context);

            String fileName = "certificate_" + certificate.getId() + ".pdf";
            File outputFile = new File(folder, fileName);

            try (FileOutputStream os = new FileOutputStream(outputFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, new File(".").toURI().toString());
                builder.toStream(os);
                builder.run();
            }

            return "/" + storagePath + "/" + fileName;
        } catch (Exception e) {
            throw new InternalServerException("Failed to generate certificate PDF");
        }
    }
}