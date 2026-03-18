package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.TeacherApplicationRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherApplicationService {

    private final TeacherApplicationRepository teacherApplicationRepository;
    private final UserRepository userRepository;

    public TeacherApplicationService(TeacherApplicationRepository teacherApplicationRepository,
                                     UserRepository userRepository) {
        this.teacherApplicationRepository = teacherApplicationRepository;
        this.userRepository = userRepository;
    }

    public TeacherApplication submitApplication(TeacherApplicationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only TEACHER users can submit teacher applications");
        }

        if (teacherApplicationRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Teacher application already exists for this user");
        }

        TeacherApplication application = new TeacherApplication();
        application.setUserId(request.getUserId());
        application.setFullName(request.getFullName());
        application.setEmail(request.getEmail());
        application.setResumeText(request.getResumeText());
        application.setResumeFileUrl(request.getResumeFileUrl());
        application.setSpecialization(request.getSpecialization());
        application.setYearsOfExperience(request.getYearsOfExperience());
        application.setStatus("PENDING");
        application.setCreatedAt(LocalDateTime.now());
        application.setScore(calculateResumeScore(request));

        return teacherApplicationRepository.save(application);
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

    private int calculateResumeScore(TeacherApplicationRequest request) {
        int score = 0;

        if (request.getResumeText() != null && request.getResumeText().length() >= 100) {
            score += 30;
        }

        if (request.getYearsOfExperience() >= 1) {
            score += 20;
        }

        if (request.getYearsOfExperience() >= 3) {
            score += 20;
        }

        String resumeLower = request.getResumeText().toLowerCase();

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