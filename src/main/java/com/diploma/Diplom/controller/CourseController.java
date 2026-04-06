package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateCourseRequest;
import com.diploma.Diplom.dto.UpdateCourseRequest;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.service.CourseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/courses")
@Tag(name = "Courses", description = "Create, update, publish, and browse courses")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(
        summary = "Create a new course (TEACHER)",
        description = "Multipart form. Optionally upload a thumbnail image.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Course created",
                content = @Content(schema = @Schema(implementation = Course.class))),
            @ApiResponse(responseCode = "403", description = "Not a teacher", content = @Content)
        }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Course createCourse(
            Authentication authentication,
            @Parameter(description = "Course title") @RequestParam String title,
            @Parameter(description = "Course description") @RequestParam String description,
            @Parameter(description = "Category e.g. 'Programming'") @RequestParam String category,
            @Parameter(description = "beginner | intermediate | advanced") @RequestParam(required = false) String level,
            @Parameter(description = "Thumbnail image file") @RequestParam(required = false) MultipartFile thumbnailFile,
            @Parameter(description = "true = free course") @RequestParam(required = false) Boolean free,
            @Parameter(description = "Price in USD (ignored when free=true)") @RequestParam(required = false) BigDecimal price
    ) {
        CreateCourseRequest request = new CreateCourseRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setLevel(level);
        request.setFree(free);
        request.setPrice(price);
        return courseService.createCourse(authentication.getName(), request, thumbnailFile);
    }

    @Operation(
        summary = "Get my courses (TEACHER)",
        description = "Returns all courses created by the authenticated teacher.",
        responses = @ApiResponse(responseCode = "200", description = "List of courses",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Course.class))))
    )
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public List<Course> getMyCourses(Authentication authentication) {
        return courseService.getTeacherCourses(authentication.getName());
    }

    @Operation(
        summary = "Get course by ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Course found",
                content = @Content(schema = @Schema(implementation = Course.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
        }
    )
    @GetMapping("/{courseId}")
    public Course getCourseById(
            @Parameter(description = "MongoDB course ID") @PathVariable String courseId) {
        return courseService.getCourseById(courseId);
    }

    @Operation(
        summary = "Update a course (TEACHER)",
        description = "All fields are optional. Only the course owner can update.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Updated",
                content = @Content(schema = @Schema(implementation = Course.class))),
            @ApiResponse(responseCode = "403", description = "Not owner", content = @Content)
        }
    )
    @PutMapping(value = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Course updateCourse(
            Authentication authentication,
            @PathVariable String courseId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @Parameter(description = "true to publish, false to unpublish")
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) MultipartFile thumbnailFile
    ) {
        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setLevel(level);
        request.setPublished(published);
        return courseService.updateCourse(authentication.getName(), courseId, request, thumbnailFile);
    }

    @Operation(
        summary = "Delete a course (TEACHER)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Not owner", content = @Content)
        }
    )
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteCourse(Authentication authentication, @PathVariable String courseId) {
        courseService.deleteCourse(authentication.getName(), courseId);
        return "Course deleted successfully";
    }

    @Operation(
        summary = "List all published courses (public)",
        description = "No authentication required. Returns courses visible to students.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Course.class))))
    )
    @GetMapping("/public")
    public List<Course> getPublicCourses() {
        return courseService.getPublicCourses();
    }
}