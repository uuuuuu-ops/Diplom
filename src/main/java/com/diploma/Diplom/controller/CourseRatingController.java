package com.diploma.Diplom.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.RatingRequest;
import com.diploma.Diplom.model.CourseRating;
import com.diploma.Diplom.service.CourseRatingService;

@RestController
@RequestMapping("/courses/{courseId}/ratings")
@Tag(name = "Courses", description = "Create, update, publish, and browse courses")
@SecurityRequirement(name = "bearerAuth")
public class CourseRatingController {

    private final CourseRatingService ratingService;

    public CourseRatingController(CourseRatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(
        summary = "Rate or update a course rating (STUDENT)",
        description = """
            A student can submit one rating per course. Calling this again updates the existing rating.

            **Requirements:** The student must have completed at least one lesson in the course.

            **Body example:**
            ```json
            { "rating": 4, "review": "Great explanations, could use more exercises." }
            ```
            `review` is optional.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Rating saved",
                content = @Content(schema = @Schema(implementation = CourseRating.class))),
            @ApiResponse(responseCode = "400",
                description = "Rating out of range (1-5) or student has not started the course",
                content = @Content),
            @ApiResponse(responseCode = "403", description = "Not a student", content = @Content)
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CourseRating> rateOrUpdate(
            @Parameter(description = "Course to rate") @PathVariable String courseId,
            @RequestBody RatingRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ratingService.rateOrUpdate(authentication.getName(), courseId, request));
    }

    @Operation(
        summary = "Get all ratings for a course (public)",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseRating.class))))
    )
    @GetMapping
    public ResponseEntity<List<CourseRating>> getRatings(@PathVariable String courseId) {
        return ResponseEntity.ok(ratingService.getRatings(courseId));
    }

    @Operation(
        summary = "Delete my rating (STUDENT)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Rating removed"),
            @ApiResponse(responseCode = "404", description = "No rating found", content = @Content)
        }
    )
    @DeleteMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> deleteRating(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        ratingService.deleteRating(authentication.getName(), courseId);
        return ResponseEntity.ok("Rating removed");
    }
}

