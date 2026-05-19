package com.diploma.Diplom.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.dto.CertificateResponse;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.util.CertificateUtils;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PdfCertificateService pdfCertificateService;

    public CertificateService(
            CertificateRepository certificateRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            PdfCertificateService pdfCertificateService
    ) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.pdfCertificateService = pdfCertificateService;
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

        certificate = certificateRepository.save(certificate);

        String pdfUrl = pdfCertificateService.generateCertificatePdf(certificate);
        certificate.setPdfUrl(pdfUrl);

        certificateRepository.save(certificate);

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
}