package com.diploma.Diplom.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.CertificateResponse;
import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.service.CertificateService;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Progress & Certificates", description = "Track lesson completion and course progress")
@SecurityRequirement(name = "bearerAuth")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @Operation(
        summary = "Issue a certificate (ADMIN / internal)",
        description = """
            Normally called automatically when a student completes all lessons and quizzes.
            Can be triggered manually by an admin if needed.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Certificate issued",
                content = @Content(schema = @Schema(implementation = CertificateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Certificate already exists",
                content = @Content)
        }
    )
    @PreAuthorize("hasRole('ADMIN')") 
    @PostMapping("/issue")
    public ResponseEntity<CertificateResponse> issueCertificate(
            @Parameter(description = "Student user ID") @RequestParam String userId,
            @Parameter(description = "Course ID") @RequestParam String courseId
    ) {
        return ResponseEntity.ok(certificateService.issueCertificate(userId, courseId));
    }

    @Operation(
        summary = "Regenerate a certificate PDF",
        description = "Re-renders the PDF for an existing certificate (e.g. after a template update).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Regenerated",
                content = @Content(schema = @Schema(implementation = CertificateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Certificate not found", content = @Content)
        }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/regenerate")
    public ResponseEntity<CertificateResponse> regenerateCertificate(
            @Parameter(description = "Certificate ID") @PathVariable String id) {
        return ResponseEntity.ok(certificateService.regenerateCertificate(id));
    }

    @Operation(
        summary = "Get a certificate by ID",
        responses = {
            @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = Certificate.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<Certificate> getCertificate(@PathVariable String id) {
        return ResponseEntity.ok(certificateService.getById(id));
    }

    @Operation(
        summary = "Verify a certificate by its verification code (public)",
        description = "Used by third parties to confirm a certificate is genuine. No JWT required.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Valid certificate",
                content = @Content(schema = @Schema(implementation = Certificate.class))),
            @ApiResponse(responseCode = "404", description = "Invalid verification code",
                content = @Content)
        }
    )
    @GetMapping("/verify/{verificationCode}")
    public ResponseEntity<Certificate> verifyCertificate(
            @Parameter(description = "UUID printed on the certificate") @PathVariable String verificationCode) {
        return ResponseEntity.ok(certificateService.verifyCertificate(verificationCode));
    }
}