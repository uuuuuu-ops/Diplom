package com.diploma.Diplom.controller;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.SubmitQuizRequest;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.QuizAttempt;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.QuizAttemptService;

@RestController
@RequestMapping("/quiz-attempts")
@Tag(name = "Quizzes", description = "Create and manage quizzes attached to lessons")
@SecurityRequirement(name = "bearerAuth")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;
    private final UserRepository userRepository;

    public QuizAttemptController(QuizAttemptService quizAttemptService,
                                  UserRepository userRepository) {
        this.quizAttemptService = quizAttemptService;
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Submit quiz answers (STUDENT)",
        description = """
            Answers are graded **server-side** — the frontend never receives correct answer indices.

            **Body example:**
            ```json
            {
              "quizId": "664abc123",
              "answers": [2, 0, 1, 3]
            }
            ```
            Each element is the index of the chosen option for that question (0-based).

            **Time limit:** Record `startedAt = new Date().toISOString()` when the student
            opens the quiz page, then pass it as the query parameter when submitting.
            The server rejects submissions that arrive more than `timeLimitSeconds + 10s` after start.
            Omit `startedAt` if the quiz has no time limit.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Attempt recorded — includes score and passed flag",
                content = @Content(schema = @Schema(implementation = QuizAttempt.class))),
            @ApiResponse(responseCode = "400",
                description = "Wrong answer count, quiz not published, or time limit exceeded",
                content = @Content),
            @ApiResponse(responseCode = "403", description = "Not a student", content = @Content)
        }
    )
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttempt> submitQuiz(
            Authentication authentication,
            @RequestBody SubmitQuizRequest request,
            @Parameter(description = "ISO-8601 timestamp when the student opened the quiz, e.g. 2024-06-01T10:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startedAt
    ) {
        String userId = getCurrentUser(authentication).getId();
        return ResponseEntity.ok(quizAttemptService.submitQuiz(userId, request, startedAt));
    }

    @Operation(
        summary = "Get my attempts for a quiz (STUDENT)",
        description = "Returns all past attempts by the authenticated student for one quiz.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuizAttempt.class))))
    )
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuizAttempt>> getMyAttempts(
            Authentication authentication,
            @Parameter(description = "Quiz ID to filter by") @RequestParam String quizId
    ) {
        String userId = getCurrentUser(authentication).getId();
        return ResponseEntity.ok(quizAttemptService.getMyAttempts(userId, quizId));
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}