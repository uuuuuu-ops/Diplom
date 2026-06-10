package com.diploma.Diplom.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.diploma.Diplom.exception.InternalServerException;
import com.diploma.Diplom.model.Certificate;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.FSStreamFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfCertificateService {

    private static final Logger log = LoggerFactory.getLogger(PdfCertificateService.class);

    private final TemplateEngine templateEngine;
    private final QrCodeService qrCodeService;
    private final CloudinaryService cloudinaryService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public PdfCertificateService(TemplateEngine templateEngine, QrCodeService qrCodeService, CloudinaryService cloudinaryService) {
        this.templateEngine = templateEngine;
        this.qrCodeService = qrCodeService;
        this.cloudinaryService = cloudinaryService;
    }

    public String generateCertificatePdf(Certificate certificate) {
        try {
            String verificationUrl = frontendBaseUrl + "/certificates/verify/" + certificate.getVerificationCode();
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

            String html = templateEngine.process("Certificate-template", context);

            String fileName = "certificate_" + certificate.getId();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Return empty stream for external HTTP/HTTPS resources (Google Fonts CDN)
            // so generation does not fail when the container has no internet access
            FSStreamFactory emptyFactory = uri -> new FSStream() {
                @Override public InputStream getStream() { return new ByteArrayInputStream(new byte[0]); }
                @Override public Reader getReader() { return new StringReader(""); }
            };
            builder.useProtocolsStreamImplementation(emptyFactory, "http", "https");
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();

            CloudinaryService.FileUploadResult uploaded = cloudinaryService.uploadBytes(os.toByteArray(), fileName, "certificates");
            return uploaded.getFileUrl();
        } catch (Exception e) {
            log.error("Failed to generate certificate PDF for certificate {}: {}", certificate.getId(), e.getMessage(), e);
            throw new InternalServerException("Failed to generate certificate PDF: " + e.getMessage());
        }
    }
}