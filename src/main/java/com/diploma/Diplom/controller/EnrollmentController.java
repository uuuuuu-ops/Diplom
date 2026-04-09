package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CourseAccessResponse;
import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.service.EnrollmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@Tag(name = "Enrollments & Payments", description = "Enroll in courses and manage payments")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @Operation(
        summary = "Enroll in a free course (STUDENT)",
        description = "Creates an ACTIVE enrollment. Throws if the course is not free — use PayPal instead.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Enrolled",
                content = @Content(schema = @Schema(implementation = Enrollment.class))),
            @ApiResponse(responseCode = "400", description = "Course is not free", content = @Content)
        }
    )
    @PostMapping("/free/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Enrollment enrollFreeCourse(
            @Parameter(description = "Free course ID") @PathVariable String courseId) {
        return enrollmentService.enrollFreeCourse(courseId);
    }

    @Operation(
        summary = "Check if the current user has access to a course",
        description = """
            Returns `{ "hasAccess": true/false }`. Access is granted when:
            - The course is free, or
            - The user has an active enrollment (purchased), or
            - The user has an active subscription.
            """,
        responses = @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = CourseAccessResponse.class)))
    )
    @GetMapping("/check/{courseId}")
    public CourseAccessResponse checkAccess(
            @Parameter(description = "Course ID to check") @PathVariable String courseId) {
        String userId = enrollmentService.getCurrentUserId();
        boolean hasAccess = enrollmentService.hasAccess(userId, courseId);
        return new CourseAccessResponse(hasAccess);
    }

    @Operation(
        summary = "Get my enrollments",
        description = "Returns all enrollments for the authenticated user.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Enrollment.class))))
    )
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public List<Enrollment> getMyEnrollments() {
        return enrollmentService.getMyEnrollments();
    }
}