package com.diploma.Diplom.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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

            String html = templateEngine.process("Certificate-template", context);

            String fileName = "certificate_" + certificate.getId() + ".pdf";
            File outputFile = new File(folder, fileName);

            String baseUri = folder.toURI().toString();
            try (FileOutputStream os = new FileOutputStream(outputFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                // Return empty stream for external HTTP/HTTPS resources (Google Fonts CDN)
                // so generation does not fail when the container has no internet access
                FSStreamFactory emptyFactory = uri -> new FSStream() {
                    @Override public InputStream getStream() { return new ByteArrayInputStream(new byte[0]); }
                    @Override public Reader getReader() { return new StringReader(""); }
                };
                builder.useProtocolsStreamImplementation(emptyFactory, "http", "https");
                builder.withHtmlContent(html, baseUri);
                builder.toStream(os);
                builder.run();
            }

            return "/" + storagePath + "/" + fileName;
        } catch (Exception e) {
            log.error("Failed to generate certificate PDF for certificate {}: {}", certificate.getId(), e.getMessage(), e);
            throw new InternalServerException("Failed to generate certificate PDF: " + e.getMessage());
        }
    }
}