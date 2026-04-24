package com.diploma.Diplom.controller;

import java.time.LocalDateTime;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.SubmitQuizRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.QuizAttempt;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.QuizAttemptService;

import java.time.Duration;

@RestController
@RequestMapping("/quiz-attempts")
@Tag(name = "Quizzes", description = "Create and manage quizzes attached to lessons")
@SecurityRequirement(name = "bearerAuth")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUIZ_SESSION_PREFIX = "quiz:session:";
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    public QuizAttemptController(QuizAttemptService quizAttemptService,
                                 UserRepository userRepository,
                                 RedisTemplate<String, String> redisTemplate) {
        this.quizAttemptService = quizAttemptService;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Operation(
        summary = "Start a quiz session (STUDENT)",
        description = """
            Call this when the student opens the quiz page.
            Records the server-side start time in Redis (TTL 2h).
            Returns the session key to pass to /submit.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Session started",
                content = @Content(schema = @Schema(example = "{\"sessionKey\": \"quiz:session:userId:quizId\"}"))),
            @ApiResponse(responseCode = "403", description = "Not a student", content = @Content)
        }
    )
    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> startQuiz(
            Authentication authentication,
            @Parameter(description = "Quiz ID") @RequestParam String quizId
    ) {
        String userId = getCurrentUser(authentication).getId();
        String sessionKey = QUIZ_SESSION_PREFIX + userId + ":" + quizId;
        redisTemplate.opsForValue().set(sessionKey, LocalDateTime.now().toString(), SESSION_TTL);
        return ResponseEntity.ok(Map.of("sessionKey", sessionKey));
    }

    @Operation(
        summary = "Submit quiz answers (STUDENT)",
        description = """
            Answers are graded **server-side** — the frontend never receives correct answer indices.

            **Required:** Call **POST /quiz-attempts/start** before this to create a server-side session.
            The start time is stored in Redis and cannot be manipulated by the client.

            **Body example:**
            ```json
            {
              "quizId": "664abc123",
              "answers": [2, 0, 1, 3]
            }
            ```
            Each element is the index of the chosen option for that question (0-based).
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Attempt recorded",
                content = @Content(schema = @Schema(implementation = QuizAttempt.class))),
            @ApiResponse(responseCode = "400",
                description = "Wrong answer count, quiz not published, time limit exceeded, or session not started",
                content = @Content),
            @ApiResponse(responseCode = "403", description = "Not a student", content = @Content)
        }
    )
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttempt> submitQuiz(
            Authentication authentication,
            @RequestBody SubmitQuizRequest request
    ) {
        String userId = getCurrentUser(authentication).getId();
        String sessionKey = QUIZ_SESSION_PREFIX + userId + ":" + request.getQuizId();

        String startedAtStr = redisTemplate.opsForValue().get(sessionKey);
        if (startedAtStr == null) {
            throw new BadRequestException(
                "No active quiz session found. Call POST /quiz-attempts/start first.");
        }

        LocalDateTime startedAt = LocalDateTime.parse(startedAtStr);

        QuizAttempt attempt = quizAttemptService.submitQuiz(userId, request, startedAt);

        redisTemplate.delete(sessionKey);

        return ResponseEntity.ok(attempt);
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
        return userRepository.findById(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}