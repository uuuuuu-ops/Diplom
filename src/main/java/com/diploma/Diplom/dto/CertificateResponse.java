package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
@Schema(description = "Response containing certificate information after generation or retrieval")
@Data
@AllArgsConstructor
public class CertificateResponse {
    @Schema(description = "The unique identifier for the certificate")
    private String id;

    @Schema(description = "The certificate number assigned to the generated certificate")
    private String certificateNumber;

    @Schema(description = "The URL to the PDF version of the certificate, which can be downloaded or viewed by the user")
    private String pdfUrl;

    @Schema(description = "The verification code for the certificate, which can be used to verify the authenticity of the certificate")
    private String verificationCode;
    
    @Schema(description = "A message related to the certificate operation, such as success or error information")
    private String message;
}