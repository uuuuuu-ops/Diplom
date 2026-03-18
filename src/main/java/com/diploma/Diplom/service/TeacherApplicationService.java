package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ResumeAnalysisResult;
import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.TeacherApplicationRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.apache.pdfbox.Loader;
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
    private final OpenAiResumeAnalysisService openAiResumeAnalysisService;

    public TeacherApplicationService(TeacherApplicationRepository teacherApplicationRepository,
                                     UserRepository userRepository,
                                     OpenAiResumeAnalysisService openaiResumeAnalysisService) {
        this.teacherApplicationRepository = teacherApplicationRepository;
        this.userRepository = userRepository;
        this.openAiResumeAnalysisService = openaiResumeAnalysisService;
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

        try {
            String uploadDir = "uploads/resumes";
            Files.createDirectories(Paths.get(uploadDir));

            String safeFileName = UUID.randomUUID() + "_" + originalFileName.replaceAll("\\s+", "_");
            Path filePath = Paths.get(uploadDir, safeFileName);

            Files.copy(resumeFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String resumeText = extractTextFromPdf(filePath);

            ResumeAnalysisResult analysis = openAiResumeAnalysisService.analyzeResume(
                    resumeText,
                    request.getSpecialization(),
                    request.getYearsOfExperience()
            );

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

            application.setScore(analysis.getScore());
            application.setAiSummary(analysis.getSummary());
            application.setAiStrengths(analysis.getStrengths());
            application.setAiWeaknesses(analysis.getWeaknesses());
            application.setAiRecommendation(analysis.getRecommendation());

            return teacherApplicationRepository.save(application);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload/process PDF: " + e.getMessage(), e);
        }
    }

    public List<TeacherApplication> getAllApplications() {
        return teacherApplicationRepository.findAll();
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

    public TeacherApplication getApplicationById(String applicationId) {
        return teacherApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Teacher application not found"));
    }

    private String extractTextFromPdf(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}