package com.diploma.Diplom.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.CompleteLessonRequest;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.service.CourseProgressService;

@RestController
@RequestMapping("/progress")
@Tag(name = "Progress & Certificates", description = "Track lesson completion and course progress")
@SecurityRequirement(name = "bearerAuth")
public class ProgressController {

    private final CourseProgressService courseProgressService;

    public ProgressController(CourseProgressService courseProgressService) {
        this.courseProgressService = courseProgressService;
    }

    @Operation(
        summary = "Mark a lesson as complete (STUDENT)",
        description = """
            Marks the lesson complete and recalculates the overall course progress percentage.

            **Quiz gate:** If the lesson has `quizRequired = true`, the student must have
            already passed the lesson's quiz — otherwise this endpoint returns 400.

            When all lessons are completed and all quizzes passed, the course is marked complete
            and a certificate is automatically issued.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Progress updated",
                content = @Content(schema = @Schema(implementation = CourseProgress.class))),
            @ApiResponse(responseCode = "400",
                description = "Quiz not yet passed (quizRequired=true)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not a student", content = @Content)
        }
    )
    @PostMapping("/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CourseProgress> completeLesson(
            @RequestBody CompleteLessonRequest request,
            Authentication authentication
    ) {
        CourseProgress progress = courseProgressService.markLessonCompleted(
                authentication.getName(), request.getCourseId(), request.getLessonId());
        return ResponseEntity.ok(progress);
    }

    @Operation(
        summary = "Get course progress (STUDENT / TEACHER / ADMIN)",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(schema = @Schema(implementation = CourseProgress.class)))
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<CourseProgress> getProgress(
            @Parameter(description = "MongoDB course ID") @RequestParam String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(courseProgressService.getProgress(authentication.getName(), courseId));
    }

    @Operation(
        summary = "Check whether a lesson is unlocked (STUDENT)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returns `{ \"unlocked\": true/false }`",
                content = @Content(schema = @Schema(example = "{\"unlocked\": true}"))),
            @ApiResponse(responseCode = "403", description = "Not a student", content = @Content)
        }
    )
    @GetMapping("/lesson-unlocked")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Boolean>> isLessonUnlocked(
            @Parameter(description = "Lesson to check") @RequestParam String lessonId,
            @Parameter(description = "Course the lesson belongs to") @RequestParam String courseId,
            Authentication authentication
    ) {
        boolean unlocked = courseProgressService.isLessonUnlocked(
                authentication.getName(), courseId, lessonId);
        return ResponseEntity.ok(Map.of("unlocked", unlocked));
    }
}
