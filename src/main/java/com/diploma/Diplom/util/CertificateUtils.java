package com.diploma.Diplom.util;

import java.util.UUID;

public class CertificateUtils {

    public static String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis();
    }

    public static String generateVerificationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}