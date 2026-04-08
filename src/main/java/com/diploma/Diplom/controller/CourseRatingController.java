package com.diploma.Diplom.controller;

import java.security.Principal;
import java.util.List;

import com.diploma.Diplom.exception.ResourceNotFoundException;
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
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.RatingRequest;
import com.diploma.Diplom.model.CourseRating;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.CourseRatingService;

@RestController
@RequestMapping("/courses/{courseId}/ratings")
@Tag(name = "Courses", description = "Create, update, publish, and browse courses")
@SecurityRequirement(name = "bearerAuth")
public class CourseRatingController {

    private final CourseRatingService ratingService;
    private final UserRepository userRepository;

    public CourseRatingController(CourseRatingService ratingService,
                                   UserRepository userRepository) {
        this.ratingService = ratingService;
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Rate or update a course rating (STUDENT)",
        description = """
            A student can submit one rating per course. Calling this again updates the existing rating.

            **Requirements:** The student must have completed at least one lesson in the course.

            After saving, `avgRating` and `ratingCount` on the Course document are automatically recalculated.

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
            Principal principal
    ) {
        String userId = getCurrentUser(principal).getId();
        return ResponseEntity.ok(ratingService.rateOrUpdate(userId, courseId, request));
    }

    @Operation(
        summary = "Get all ratings for a course (public)",
        description = "No authentication required. Returns every student rating with optional review text.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseRating.class))))
    )
    @GetMapping
    public ResponseEntity<List<CourseRating>> getRatings(
            @PathVariable String courseId) {
        return ResponseEntity.ok(ratingService.getRatings(courseId));
    }

    @Operation(
        summary = "Delete my rating (STUDENT)",
        description = "Removes the authenticated student's rating and recalculates the course average.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Rating removed"),
            @ApiResponse(responseCode = "404", description = "No rating found for this student",
                content = @Content)
        }
    )
    @DeleteMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> deleteRating(
            @PathVariable String courseId,
            Principal principal
    ) {
        String userId = getCurrentUser(principal).getId();
        ratingService.deleteRating(userId, courseId);
        return ResponseEntity.ok("Rating removed");
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}