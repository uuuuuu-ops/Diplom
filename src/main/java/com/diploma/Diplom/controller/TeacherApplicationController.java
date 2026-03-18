package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.dto.TeacherReviewRequest;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.service.TeacherApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher-applications")
public class TeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;

    public TeacherApplicationController(TeacherApplicationService teacherApplicationService) {
        this.teacherApplicationService = teacherApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherApplication submitApplication(@Valid @RequestBody TeacherApplicationRequest request) {
        return teacherApplicationService.submitApplication(request);
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
}