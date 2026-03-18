package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.repository.CertificateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;

    public CertificateService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    public Certificate issueCertificate(Certificate certificate) {
        return certificateRepository.save(certificate);
    }

    public List<Certificate> getCertificatesByUser(String userId) {
        return certificateRepository.findByUserId(userId);
    }
}