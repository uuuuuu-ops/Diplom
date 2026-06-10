package com.diploma.Diplom.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.diploma.Diplom.dto.CertificateResponse;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.repository.CourseProgressRepository;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.util.CertificateUtils;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PdfCertificateService pdfCertificateService;
    private final CourseProgressRepository courseProgressRepository;

    public CertificateService(
            CertificateRepository certificateRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            PdfCertificateService pdfCertificateService,
            CourseProgressRepository courseProgressRepository
    ) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.pdfCertificateService = pdfCertificateService;
        this.courseProgressRepository = courseProgressRepository;
    }

    public CertificateResponse issueCertificate(String userId, String courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Resolve real instructor name
        String instructorName = userRepository.findById(course.getTeacherId())
                .map(User::getName)
                .orElse("Course Instructor");

        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setCourseId(courseId);
        certificate.setStudentName(user.getName());
        certificate.setCourseTitle(course.getTitle());
        certificate.setInstructorName(instructorName);
        certificate.setCertificateNumber(CertificateUtils.generateCertificateNumber());
        certificate.setVerificationCode(CertificateUtils.generateVerificationCode());
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setTemplateVersion("v1");
        certificate.setActive(true);

        Certificate saved = certificateRepository.save(certificate);

        try {
            String pdfUrl = pdfCertificateService.generateCertificatePdf(saved);
            saved.setPdfUrl(pdfUrl);
            certificateRepository.save(saved);
        } catch (Exception e) {
            // PDF generation failed; certificate record is saved without pdfUrl
        }

        courseProgressRepository.findFirstByUserIdAndCourseId(userId, courseId)
                .ifPresent(progress -> {
                    progress.setCertificateId(saved.getId());
                    courseProgressRepository.save(progress);
                });

        certificate = saved;

        return new CertificateResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getPdfUrl(),
                certificate.getVerificationCode(),
                "Certificate issued successfully"
        );
    }

    public CertificateResponse regenerateCertificate(String certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        User user = userRepository.findById(certificate.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Course course = courseRepository.findById(certificate.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        certificate.setStudentName(user.getName());
        certificate.setCourseTitle(course.getTitle());
        certificate.setRegeneratedAt(LocalDateTime.now());
        certificate.setTemplateVersion("v2");

        String pdfUrl = pdfCertificateService.generateCertificatePdf(certificate);
        certificate.setPdfUrl(pdfUrl);

        certificateRepository.save(certificate);

        return new CertificateResponse(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getPdfUrl(),
                certificate.getVerificationCode(),
                "Certificate regenerated successfully"
        );
    }

    public Certificate verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    }

    public Certificate getById(String id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    }

    public java.util.List<Certificate> getMyCertificates(String userId) {
        return certificateRepository.findByUserId(userId);
    }

    public CertificateResponse claimCertificate(String userId, String courseId) {
        java.util.Optional<Certificate> existing = certificateRepository.findByUserIdAndCourseId(userId, courseId);

        if (existing.isPresent()) {
            Certificate cert = existing.get();
            String pdfError = null;
            if (cert.getPdfUrl() == null) {
                try {
                    String pdfUrl = pdfCertificateService.generateCertificatePdf(cert);
                    cert.setPdfUrl(pdfUrl);
                    certificateRepository.save(cert);
                } catch (Exception e) {
                    pdfError = e.getMessage();
                    log.error("PDF regeneration failed for certificate {}: {}", cert.getId(), e.getMessage(), e);
                }
            }
            String msg = cert.getPdfUrl() != null ? "Certificate retrieved"
                    : "PDF generation failed: " + pdfError;
            return new CertificateResponse(
                    cert.getId(),
                    cert.getCertificateNumber(),
                    cert.getPdfUrl(),
                    cert.getVerificationCode(),
                    msg);
        }

        boolean completed = courseProgressRepository
                .findFirstByUserIdAndCourseId(userId, courseId)
                .map(com.diploma.Diplom.model.CourseProgress::isCompleted)
                .orElse(false);
        if (!completed) {
            throw new com.diploma.Diplom.exception.ForbiddenException("Course is not yet completed");
        }
        return issueCertificate(userId, courseId);
    }
}