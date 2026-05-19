package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CertificateResponse;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock CertificateRepository certificateRepository;
    @Mock UserRepository userRepository;
    @Mock CourseRepository courseRepository;
    @Mock PdfCertificateService pdfCertificateService;

    @InjectMocks CertificateService certificateService;

    private User makeUser() {
        User u = new User();
        u.setId("user-1");
        u.setName("John Doe");
        return u;
    }

    private Course makeCourse() {
        Course c = new Course();
        c.setId("course-1");
        c.setTitle("Java Basics");
        return c;
    }

    @Test
    void issueCertificate_success_returnsCertificateResponse() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(makeUser()));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(makeCourse()));
        when(certificateRepository.save(any())).thenAnswer(inv -> {
            Certificate cert = inv.getArgument(0);
            cert.setId("cert-1");
            return cert;
        });
        when(pdfCertificateService.generateCertificatePdf(any())).thenReturn("https://cdn.example.com/cert.pdf");

        CertificateResponse response = certificateService.issueCertificate("user-1", "course-1");

        assertThat(response).isNotNull();
        assertThat(response.getPdfUrl()).isEqualTo("https://cdn.example.com/cert.pdf");
        assertThat(response.getMessage()).isEqualTo("Certificate issued successfully");
        assertThat(response.getCertificateNumber()).isNotNull();
        verify(certificateRepository, times(2)).save(any()); // первый save + второй с pdfUrl
    }

    @Test
    void issueCertificate_userNotFound_throws() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.issueCertificate("missing", "course-1"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void issueCertificate_courseNotFound_throws() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(makeUser()));
        when(courseRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.issueCertificate("user-1", "missing"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Course not found");
    }

    @Test
    void verifyCertificate_validCode_returnsCertificate() {
        Certificate cert = new Certificate();
        cert.setVerificationCode("ABC-123");
        cert.setStudentName("John Doe");

        when(certificateRepository.findByVerificationCode("ABC-123")).thenReturn(Optional.of(cert));

        Certificate result = certificateService.verifyCertificate("ABC-123");

        assertThat(result.getVerificationCode()).isEqualTo("ABC-123");
        assertThat(result.getStudentName()).isEqualTo("John Doe");
    }

    @Test
    void verifyCertificate_invalidCode_throws() {
        when(certificateRepository.findByVerificationCode("WRONG")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.verifyCertificate("WRONG"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_found_returnsCertificate() {
        Certificate cert = new Certificate();
        cert.setId("cert-1");
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));

        Certificate result = certificateService.getById("cert-1");

        assertThat(result.getId()).isEqualTo("cert-1");
    }

    @Test
    void getById_notFound_throws() {
        when(certificateRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getById("missing"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void regenerateCertificate_success_updatesTemplateVersion() {
        Certificate cert = new Certificate();
        cert.setId("cert-1");
        cert.setUserId("user-1");
        cert.setCourseId("course-1");
        cert.setTemplateVersion("v1");

        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(makeUser()));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(makeCourse()));
        when(pdfCertificateService.generateCertificatePdf(any())).thenReturn("https://cdn.example.com/new.pdf");
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse response = certificateService.regenerateCertificate("cert-1");

        assertThat(cert.getTemplateVersion()).isEqualTo("v2");
        assertThat(response.getPdfUrl()).isEqualTo("https://cdn.example.com/new.pdf");
        assertThat(response.getMessage()).isEqualTo("Certificate regenerated successfully");
    }
}