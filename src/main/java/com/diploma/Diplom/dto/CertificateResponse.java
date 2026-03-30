package com.diploma.Diplom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CertificateResponse {
    private String id;
    private String certificateNumber;
    private String pdfUrl;
    private String verificationCode;
    private String message;
}