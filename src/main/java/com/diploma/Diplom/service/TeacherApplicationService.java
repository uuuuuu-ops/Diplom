package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ResumeAnalysisResult;
import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.InternalServerException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.TeacherApplicationRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherApplicationService {

    private final TeacherApplicationRepository teacherApplicationRepository;
    private final UserRepository userRepository;
    private final OpenAiResumeAnalysisService openAiResumeAnalysisService;
    private final CloudinaryService cloudinaryService; 

    public TeacherApplicationService(TeacherApplicationRepository teacherApplicationRepository,
                                     UserRepository userRepository,
                                     OpenAiResumeAnalysisService openAiResumeAnalysisService,
                                     CloudinaryService cloudinaryService) { 
        this.teacherApplicationRepository = teacherApplicationRepository;
        this.userRepository = userRepository;
        this.openAiResumeAnalysisService = openAiResumeAnalysisService;
        this.cloudinaryService = cloudinaryService;
    }

    public TeacherApplication submitApplication(TeacherApplicationRequest request, MultipartFile resumeFile) {
        String userId = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only TEACHER users can submit teacher applications");
        }

        if (teacherApplicationRepository.findByUserId(user.getId()).isPresent()) {
            throw new BadRequestException("Teacher application already exists for this user");
        }

        if (resumeFile == null || resumeFile.isEmpty()) {
            throw new BadRequestException("Resume PDF file is required");
        }

        String originalFileName = resumeFile.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new ForbiddenException("Only PDF files are allowed");
        }

        try {
            String resumeText = extractTextFromPdfStream(resumeFile.getInputStream());

            CloudinaryService.FileUploadResult uploaded =
                    cloudinaryService.uploadFile(resumeFile, "resumes");

            ResumeAnalysisResult analysis = openAiResumeAnalysisService.analyzeResume(
                    resumeText,
                    request.getSpecialization(),
                    request.getYearsOfExperience()
            );

            TeacherApplication application = new TeacherApplication();
            application.setUserId(user.getId());
            application.setFullName(request.getFullName());
            application.setEmail(user.getEmail());
            application.setResumeText(resumeText);
            application.setResumeFileName(originalFileName);
            application.setResumeFileUrl(uploaded.getFileUrl());       
            application.setResumePublicId(uploaded.getPublicId());     
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
            throw new InternalServerException("Failed to upload/process PDF: " + e.getMessage());
        }
    }

    private String extractTextFromPdfStream(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
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
                .orElseThrow(() -> new ResourceNotFoundException("Teacher application not found"));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        application.setStatus("APPROVED");
        application.setReviewComment(reviewComment);

        user.setTeacherApproved(true);
        user.setRole(Role.TEACHER);

        userRepository.save(user);
        return teacherApplicationRepository.save(application);
    }

    public TeacherApplication rejectApplication(String applicationId, String reviewComment) {
        TeacherApplication application = teacherApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher application not found"));

        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        application.setStatus("REJECTED");
        application.setReviewComment(reviewComment);

        user.setTeacherApproved(false);

        userRepository.save(user);
        return teacherApplicationRepository.save(application);
    }

    public TeacherApplication getApplicationById(String applicationId) {
        return teacherApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher application not found"));
    }

    public TeacherApplication getMyApplication(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teachers can view teacher application status");
        }

        return teacherApplicationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }
}