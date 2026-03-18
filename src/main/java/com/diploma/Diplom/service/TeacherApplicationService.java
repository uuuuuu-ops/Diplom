package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.TeacherApplicationRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TeacherApplicationService {

    private final TeacherApplicationRepository teacherApplicationRepository;
    private final UserRepository userRepository;

    public TeacherApplicationService(TeacherApplicationRepository teacherApplicationRepository,
                                     UserRepository userRepository) {
        this.teacherApplicationRepository = teacherApplicationRepository;
        this.userRepository = userRepository;
    }

    public TeacherApplication submitApplication(TeacherApplicationRequest request, MultipartFile resumeFile) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only TEACHER users can submit teacher applications");
        }

        if (teacherApplicationRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Teacher application already exists for this user");
        }

        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new RuntimeException("Resume PDF file is required");
        }

        String originalFileName = resumeFile.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are allowed");
        }

        String contentType = resumeFile.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/pdf")) {
            throw new RuntimeException("Only PDF files are allowed");
        }

        try {
            String uploadDir = "uploads/resumes";
            Files.createDirectories(Paths.get(uploadDir));

            String safeFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");
            Path filePath = Paths.get(uploadDir, safeFileName);

            Files.copy(resumeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String resumeText = extractTextFromPdf(filePath);

            TeacherApplication application = new TeacherApplication();
            application.setUserId(request.getUserId());
            application.setFullName(request.getFullName());
            application.setEmail(request.getEmail());
            application.setResumeText(resumeText);
            application.setResumeFileName(originalFileName);
            application.setResumeFileUrl(filePath.toString());
            application.setSpecialization(request.getSpecialization());
            application.setYearsOfExperience(request.getYearsOfExperience());
            application.setStatus("PENDING");
            application.setCreatedAt(LocalDateTime.now());
            application.setScore(calculateResumeScore(resumeText, request.getYearsOfExperience()));

            return teacherApplicationRepository.save(application);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload and process PDF: " + e.getMessage());
        }
    }

    public List<TeacherApplication> getAllApplications() {
        return teacherApplicationRepository.findAll();
    }

    public TeacherApplication getApplicationById(String applicationId) {
    return teacherApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    public List<TeacherApplication> getPendingApplications() {
        return teacherApplicationRepository.findByStatus("PENDING");
    }

    public TeacherApplication approveApplication(String applicationId, String reviewComment) {
        TeacherApplication application = teacherApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Teacher application not found"));

        application.setStatus("APPROVED");
        application.setReviewComment(reviewComment);

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTeacherApproved(true);
        userRepository.save(user);

        return teacherApplicationRepository.save(application);
    }

    public TeacherApplication rejectApplication(String applicationId, String reviewComment) {
        TeacherApplication application = teacherApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Teacher application not found"));

        application.setStatus("REJECTED");
        application.setReviewComment(reviewComment);

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTeacherApproved(false);
        userRepository.save(user);

        return teacherApplicationRepository.save(application);
    }

    private String extractTextFromPdf(Path filePath) throws IOException {
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private int calculateResumeScore(String resumeText, int yearsOfExperience) {
        int score = 0;

        if (resumeText != null && resumeText.length() >= 100) {
            score += 30;
        }

        if (yearsOfExperience >= 1) {
            score += 20;
        }

        if (yearsOfExperience >= 3) {
            score += 20;
        }

        String resumeLower = resumeText == null ? "" : resumeText.toLowerCase();

        if (resumeLower.contains("java")) score += 10;
        if (resumeLower.contains("spring")) score += 10;
        if (resumeLower.contains("teaching")) score += 10;
        if (resumeLower.contains("education")) score += 10;

        if (score > 100) {
            score = 100;
        }

        return score;
    }
}