package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.LikeResponse;
import com.diploma.Diplom.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "Likes", description = "Course like/unlike system")
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "Toggle like for a course",
            description = "Like or unlike a course. If user already liked it → unlike, otherwise like."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully toggled like"),
            @ApiResponse(responseCode = "401", description = "Unauthorized user"),
            @ApiResponse(responseCode = "404", description = "Course not found")
    })
    @PostMapping("/{courseId}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @Parameter(description = "Course ID", example = "64f1a2b9c1234567890abcd")
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userId = authentication.getName();

        LikeResponse response = likeService.toggle(courseId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get like status and total likes",
            description = "Returns whether current user liked the course and total likes count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved like status"),
            @ApiResponse(responseCode = "401", description = "Unauthorized user")
    })
    @GetMapping("/{courseId}/like/status")
    public ResponseEntity<LikeResponse> getLikeStatus(
            @Parameter(description = "Course ID", example = "64f1a2b9c1234567890abcd")
            @PathVariable String courseId,
            Authentication authentication
    ) {
        String userId = authentication.getName();

        LikeResponse response = likeService.getStatus(courseId, userId);
        return ResponseEntity.ok(response);
    }
}