package com.diploma.Diplom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.CertificateResponse;
import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.service.CertificateService;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @PostMapping("/issue")
    public ResponseEntity<CertificateResponse> issueCertificate(
            @RequestParam String userId,
            @RequestParam String courseId
    ) {
        return ResponseEntity.ok(certificateService.issueCertificate(userId, courseId));
    }

    @PostMapping("/{id}/regenerate")
    public ResponseEntity<CertificateResponse> regenerateCertificate(@PathVariable String id) {
        return ResponseEntity.ok(certificateService.regenerateCertificate(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificate> getCertificate(@PathVariable String id) {
        return ResponseEntity.ok(certificateService.getById(id));
    }

    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<Certificate> verifyCertificate(@PathVariable String verificationCode) {
        return ResponseEntity.ok(certificateService.verifyCertificate(verificationCode));
    }
}