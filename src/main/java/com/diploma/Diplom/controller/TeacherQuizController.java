package com.diploma.Diplom.controller;

import com.diploma.Diplom.model.TeacherQuizAttempt;
import com.diploma.Diplom.model.TeacherQuizQuestion;
import com.diploma.Diplom.service.TeacherQuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/teacher-applications")
@Tag(name = "Teacher Quiz", description = "Quiz flow for teacher applications")
@SecurityRequirement(name = "bearerAuth")
public class TeacherQuizController {

    private final TeacherQuizService quizService;

    public TeacherQuizController(TeacherQuizService quizService) {
        this.quizService = quizService;
    }

    @Operation(
        summary = "Get teacher quiz questions",
        description = "Returns all quiz questions for the specified teacher application.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questions retrieved",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = TeacherQuizQuestion.class)))),
            @ApiResponse(responseCode = "404", description = "Application or quiz not found",
                content = @Content)
        }
    )
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/{applicationId}/questions")
    public ResponseEntity<List<TeacherQuizQuestion>> getQuestions(
            @Parameter(description = "Teacher application ID") @PathVariable String applicationId) {
        return ResponseEntity.ok(quizService.getQuestions(applicationId));
    }

    @Operation(
        summary = "Submit teacher quiz answers",
        description = """
            Submits quiz answers for the current authenticated user.

            **Body example:**
            ```json
            {
              "0": 2,
              "1": 1,
              "2": 3
            }
            ```

            Where:
            - key = question index or question identifier used by your service
            - value = selected answer index
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Quiz submitted successfully",
                content = @Content(schema = @Schema(implementation = TeacherQuizAttempt.class))),
            @ApiResponse(responseCode = "400", description = "Invalid answers payload",
                content = @Content),
            @ApiResponse(responseCode = "404", description = "Application or questions not found",
                content = @Content)
        }
    )
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{applicationId}/submit")
    public ResponseEntity<TeacherQuizAttempt> submitQuiz(
            @Parameter(description = "Teacher application ID") @PathVariable String applicationId,
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, Integer> answers) {

        return ResponseEntity.ok(
            quizService.submitQuiz(userId , applicationId, answers)
        );
    }

    @Operation(
        summary = "Get my teacher quiz result",
        description = "Returns the current user's quiz attempt/result for the specified application.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Quiz result retrieved",
                content = @Content(schema = @Schema(implementation = TeacherQuizAttempt.class))),
            @ApiResponse(responseCode = "404", description = "Attempt not found",
                content = @Content)
        }
    )
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/{applicationId}/result")
    public ResponseEntity<TeacherQuizAttempt> getResult(
            @Parameter(description = "Teacher application ID") @PathVariable String applicationId) {
        return ResponseEntity.ok(quizService.getMyAttempt(applicationId));
    }
}