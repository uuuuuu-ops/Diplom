package com.diploma.Diplom.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.repository.CertificateRepository;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateRepository certificateRepository;

    public CertificateController(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public List<Certificate> getCertificatesByUser(@PathVariable String userId) {
        return certificateRepository.findByUserId(userId);
    }
}