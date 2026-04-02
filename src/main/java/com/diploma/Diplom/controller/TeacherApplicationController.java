package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.dto.TeacherReviewRequest;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.service.TeacherApplicationService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/teacher-applications")
public class TeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;

    public TeacherApplicationController(TeacherApplicationService teacherApplicationService) {
        this.teacherApplicationService = teacherApplicationService;
    }

 @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@PreAuthorize("hasRole('TEACHER')")
public TeacherApplication submitApplication(
        java.security.Principal principal,       
        @RequestParam("fullName") String fullName,
        @RequestParam("email") String email,
        @RequestParam("specialization") String specialization,
        @RequestParam("yearsOfExperience") int yearsOfExperience,
        @RequestParam("resumeFile") MultipartFile resumeFile
) {
    TeacherApplicationRequest request = new TeacherApplicationRequest();
    request.setUserId(principal.getName()); 
    request.setFullName(fullName);
    request.setEmail(email);
    request.setSpecialization(specialization);
    request.setYearsOfExperience(yearsOfExperience);

    return teacherApplicationService.submitApplication(request, resumeFile);
}

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TeacherApplication> getAllApplications() {
        return teacherApplicationService.getAllApplications();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TeacherApplication> getPendingApplications() {
        return teacherApplicationService.getPendingApplications();
    }

    @PostMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherApplication approveApplication(@PathVariable String applicationId,
                                                 @RequestBody TeacherReviewRequest request) {
        return teacherApplicationService.approveApplication(applicationId, request.getReviewComment());
    }

    @PostMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherApplication rejectApplication(@PathVariable String applicationId,
                                                @RequestBody TeacherReviewRequest request) {
        return teacherApplicationService.rejectApplication(applicationId, request.getReviewComment());
    }

    @GetMapping("/{applicationId}/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getFileByApplicationId(@PathVariable String applicationId) {
        TeacherApplication application = teacherApplicationService.getApplicationById(applicationId);

        Path file = Paths.get(application.getResumeFileUrl());
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFileName().toString() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
            .body(resource);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherApplication getMyApplication(java.security.Principal principal) {
        return teacherApplicationService.getMyApplication(principal.getName());
    }
}