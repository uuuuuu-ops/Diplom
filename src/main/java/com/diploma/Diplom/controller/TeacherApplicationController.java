package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.TeacherApplicationRequest;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.TeacherApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/teacher-applications")
@Tag(name = "Teacher Applications", description = "Apply to become an approved teacher; admin review")
@SecurityRequirement(name = "bearerAuth")
public class TeacherApplicationController {

    private final TeacherApplicationService teacherApplicationService;
    private final UserRepository userRepository;

    public TeacherApplicationController(TeacherApplicationService teacherApplicationService,
                                        UserRepository userRepository) {
        this.teacherApplicationService = teacherApplicationService;
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Submit a teacher application (TEACHER role required)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Application submitted",
                content = @Content(schema = @Schema(implementation = TeacherApplication.class))),
            @ApiResponse(responseCode = "403", description = "Not a teacher role", content = @Content)
        }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherApplication submitApplication(
            Principal principal,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("specialization") String specialization,
            @Parameter(description = "Years of professional experience")
            @RequestParam("yearsOfExperience") int yearsOfExperience,
            @Parameter(description = "Resume PDF file")
            @RequestParam("resumeFile") MultipartFile resumeFile
    ) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TeacherApplicationRequest request = new TeacherApplicationRequest();
        request.setUserId(user.getId()); 
        request.setFullName(fullName);
        request.setEmail(email);
        request.setSpecialization(specialization);
        request.setYearsOfExperience(yearsOfExperience);

        return teacherApplicationService.submitApplication(request, resumeFile);
    }

    @Operation(
        summary = "Get all applications (ADMIN)",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeacherApplication.class))))
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TeacherApplication> getAllApplications() {
        return teacherApplicationService.getAllApplications();
    }

    @Operation(
        summary = "Get pending applications (ADMIN)",
        description = "Returns only applications awaiting review.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeacherApplication.class))))
    )
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TeacherApplication> getPendingApplications() {
        return teacherApplicationService.getPendingApplications();
    }

    @Operation(
        summary = "Approve application (ADMIN)",
        description = "Approves teacher application and marks user as approved teacher.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Application approved",
                content = @Content(schema = @Schema(implementation = TeacherApplication.class))),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content)
        }
    )
    @PostMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherApplication approveApplication(
            @Parameter(description = "Application ID") @PathVariable String applicationId,
            @RequestParam(value = "reviewComment", required = false) String reviewComment
    ) {
        return teacherApplicationService.approveApplication(applicationId, reviewComment);
    }

    @Operation(
        summary = "Reject application (ADMIN)",
        description = "Rejects teacher application.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Application rejected",
                content = @Content(schema = @Schema(implementation = TeacherApplication.class))),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content)
        }
    )
    @PostMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public TeacherApplication rejectApplication(
            @Parameter(description = "Application ID") @PathVariable String applicationId,
            @RequestParam(value = "reviewComment", required = false) String reviewComment
    ) {
        return teacherApplicationService.rejectApplication(applicationId, reviewComment);
    }

    @Operation(
        summary = "Download an applicant's resume PDF (ADMIN)",
        responses = {
            @ApiResponse(responseCode = "200", description = "PDF file",
                content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
        }
    )
    @GetMapping("/{applicationId}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadResume(@PathVariable String applicationId) throws Exception {
        TeacherApplication application = teacherApplicationService.getApplicationById(applicationId);

        Resource resource = new UrlResource(URI.create(application.getResumeFileUrl()));

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("Resume file not found");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + application.getResumeFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}