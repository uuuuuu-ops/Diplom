package com.diploma.Diplom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.CommentRequest;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.LessonComment;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.LessonCommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/lessons/{lessonId}/comments")
@Tag(name = "Lesson Comments", description = "Comments and replies for lessons")
@SecurityRequirement(name = "bearerAuth")
public class LessonCommentController {

    private final LessonCommentService commentService;
    private final UserRepository userRepository;

    public LessonCommentController(LessonCommentService commentService,
                                    UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Add comment or reply to lesson",
        description = """
            Adds a new comment to a lesson.

            - For a top-level comment, send `parentId: null`
            - For a reply, send `parentId` as the parent comment id

            **Body example:**
            ```json
            {
              "content": "Can you explain this topic again?",
              "parentId": null
            }
            ```
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Comment created",
                content = @Content(schema = @Schema(implementation = LessonComment.class))),
            @ApiResponse(responseCode = "403", description = "Only STUDENT or TEACHER can comment",
                content = @Content),
            @ApiResponse(responseCode = "404", description = "Lesson or user not found",
                content = @Content)
        }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<LessonComment> addComment(
            @Parameter(description = "Lesson ID") @PathVariable String lessonId,
            @RequestBody CommentRequest request,
            Principal principal
    ) {
        String userId = getCurrentUser(principal).getId();
        return ResponseEntity.ok(commentService.addComment(userId, lessonId, request));
    }

    @Operation(
        summary = "Get all lesson comments",
        description = "Returns all top-level comments for a lesson.",
        responses = @ApiResponse(responseCode = "200",
            description = "Comments retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonComment.class))))
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<List<LessonComment>> getComments(
            @Parameter(description = "Lesson ID") @PathVariable String lessonId) {
        return ResponseEntity.ok(commentService.getComments(lessonId));
    }

    @Operation(
        summary = "Get replies for a comment",
        description = "Returns all replies for the given parent comment.",
        responses = @ApiResponse(responseCode = "200",
            description = "Replies retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LessonComment.class))))
    )
    @GetMapping("/{commentId}/replies")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<List<LessonComment>> getReplies(
            @Parameter(description = "Lesson ID") @PathVariable String lessonId,
            @Parameter(description = "Parent comment ID") @PathVariable String commentId
    ) {
        return ResponseEntity.ok(commentService.getReplies(commentId));
    }

    @Operation(
        summary = "Mark comment as teacher answer",
        description = "Marks a comment as the accepted teacher answer for the lesson discussion.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Comment marked as answer",
                content = @Content(schema = @Schema(implementation = LessonComment.class))),
            @ApiResponse(responseCode = "403", description = "Only TEACHER can mark answer",
                content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found",
                content = @Content)
        }
    )
    @PatchMapping("/{commentId}/mark-answer")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LessonComment> markAsAnswer(
            @Parameter(description = "Lesson ID") @PathVariable String lessonId,
            @Parameter(description = "Comment ID") @PathVariable String commentId,
            Principal principal
    ) {
        return ResponseEntity.ok(commentService.markAsAnswer(principal.getName(), commentId));
    }

    @Operation(
        summary = "Delete own comment",
        description = "Deletes the authenticated user's comment or reply.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Comment deleted"),
            @ApiResponse(responseCode = "403", description = "User cannot delete this comment",
                content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found",
                content = @Content)
        }
    )
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER')")
    public ResponseEntity<String> deleteComment(
            @Parameter(description = "Lesson ID") @PathVariable String lessonId,
            @Parameter(description = "Comment ID") @PathVariable String commentId,
            Principal principal
    ) {
        String userId = getCurrentUser(principal).getId();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.ok("Comment deleted");
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}