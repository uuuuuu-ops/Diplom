package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateQuizRequest;
import com.diploma.Diplom.dto.UpdateQuizRequest;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.service.QuizService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@Tag(name = "Quizzes", description = "Create and manage quizzes attached to lessons")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @Operation(
        summary = "Create a quiz for a lesson (TEACHER)",
        description = """
            One quiz per lesson. The quiz is created unpublished — call PUT to publish it.

            **Body example:**
            ```json
            {
              "title": "Variables quiz",
              "description": "Test your understanding of variables",
              "passingScore": 70,
              "timeLimitSeconds": 300,
              "questions": [
                {
                  "question": "What keyword declares a constant in Java?",
                  "options": ["var", "let", "final", "const"],
                  "correctAnswerIndex": 2
                }
              ]
            }
            ```
            `timeLimitSeconds` is optional — omit it for no time limit.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Quiz created",
                content = @Content(schema = @Schema(implementation = Quiz.class))),
            @ApiResponse(responseCode = "400", description = "Quiz already exists for this lesson",
                content = @Content),
            @ApiResponse(responseCode = "403", description = "Not course owner", content = @Content)
        }
    )
    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Quiz createQuiz(
            Authentication authentication,
            @Parameter(description = "Lesson to attach the quiz to") @PathVariable String lessonId,
            @RequestBody CreateQuizRequest request
    ) {
        return quizService.createQuiz(authentication.getName(), lessonId, request);
    }

    @Operation(
        summary = "Get a quiz by ID",
        description = "Questions are returned **without** correctAnswerIndex so students cannot cheat.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Quiz found",
                content = @Content(schema = @Schema(implementation = Quiz.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
        }
    )
    @GetMapping("/{quizId}")
    public Quiz getQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    @Operation(
        summary = "Get the quiz for a specific lesson",
        responses = {
            @ApiResponse(responseCode = "200", description = "Quiz found",
                content = @Content(schema = @Schema(implementation = Quiz.class))),
            @ApiResponse(responseCode = "404", description = "No quiz for this lesson",
                content = @Content)
        }
    )
    @GetMapping("/lesson/{lessonId}")
    public Quiz getQuizByLessonId(@PathVariable String lessonId) {
        return quizService.getQuizByLessonId(lessonId);
    }

    @Operation(
        summary = "Update a quiz (TEACHER)",
        description = """
            All fields are optional. To **remove** the time limit send `"timeLimitSeconds": -1`.

            Set `"published": true` to make the quiz visible to students.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Quiz updated",
                content = @Content(schema = @Schema(implementation = Quiz.class))),
            @ApiResponse(responseCode = "403", description = "Not course owner", content = @Content)
        }
    )
    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Quiz updateQuiz(
            Authentication authentication,
            @PathVariable String quizId,
            @RequestBody UpdateQuizRequest request
    ) {
        return quizService.updateQuiz(authentication.getName(), quizId, request);
    }

    @Operation(
        summary = "Delete a quiz (TEACHER)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Not course owner", content = @Content)
        }
    )
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteQuiz(Authentication authentication, @PathVariable String quizId) {
        quizService.deleteQuiz(authentication.getName(), quizId);
        return "Quiz deleted successfully";
    }
}