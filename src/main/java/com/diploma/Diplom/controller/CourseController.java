package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateCourseRequest;
import com.diploma.Diplom.dto.UpdateCourseRequest;
import com.diploma.Diplom.exception.BadRequestException;
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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    private Authentication resolveAuthentication(Authentication authentication) {
        if (authentication != null) {
            return authentication;
        }

        Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
        if (contextAuth != null) {
            return contextAuth;
        }

        throw new AuthenticationCredentialsNotFoundException("Unauthorized");
    }

    private String getEmail(Authentication authentication) {
        Authentication auth = resolveAuthentication(authentication);

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }

        return name;
    }

    private boolean hasRole(Authentication authentication, String role) {
        Authentication auth = resolveAuthentication(authentication);
        String expected = "ROLE_" + role;

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> expected.equals(a.getAuthority()));
    }

    private void requireTeacher(Authentication authentication) {
        Authentication auth = resolveAuthentication(authentication);

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (!hasRole(auth, "TEACHER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    private void validateCreateCourseFields(String title, String description, String category) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title is required");
        }
        if (description == null || description.isBlank()) {
            throw new BadRequestException("Description is required");
        }
        if (category == null || category.isBlank()) {
            throw new BadRequestException("Category is required");
        }
    }

    @Operation(
        summary = "Create a new course (TEACHER)",
        description = "Multipart form. Optionally upload a thumbnail image.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Course created",
                content = @Content(schema = @Schema(implementation = Course.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not a teacher", content = @Content)
        }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Course createCourse(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "level", required = false) String level,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "free", required = false) Boolean free,
            @RequestParam(value = "price", required = false) BigDecimal price
    ) {
        requireTeacher(null);
        validateCreateCourseFields(title, description, category);

        CreateCourseRequest request = new CreateCourseRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setLevel(level);
        request.setFree(free);
        request.setPrice(price);

        return courseService.createCourse(getEmail(null), request, thumbnailFile);
    }

    @Operation(
        summary = "Get my courses (TEACHER)",
        description = "Returns all courses created by the authenticated teacher.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of courses",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Course.class)))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
        }
    )
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public List<Course> getMyCourses(Authentication authentication) {
        requireTeacher(authentication);
        return courseService.getTeacherCourses(getEmail(authentication));
    }

    @Operation(
        summary = "Get course by ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Course found",
                content = @Content(schema = @Schema(implementation = Course.class))
            ),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
        }
    )
    @GetMapping("/{courseId}")
    public Course getCourseById(
            @Parameter(description = "MongoDB course ID")
            @PathVariable String courseId
    ) {
        return courseService.getCourseById(courseId);
    }

    @Operation(
        summary = "Update a course (TEACHER)",
        description = "All fields are optional. Only the course owner can update.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Updated",
                content = @Content(schema = @Schema(implementation = Course.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
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

        return courseService.updateCourse(getEmail(authentication), courseId, request, thumbnailFile);
    }

    @Operation(
        summary = "Delete a course (TEACHER)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not owner", content = @Content)
        }
    )
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteCourse(Authentication authentication, @PathVariable String courseId) {
        courseService.deleteCourse(getEmail(authentication), courseId);
        return "Course deleted successfully";
    }

    @Operation(
        summary = "List all published courses (public)",
        description = "No authentication required. Returns courses visible to students.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Published courses",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Course.class)))
            )
        }
    )
    @GetMapping("/public")
    public List<Course> getPublicCourses() {
        return courseService.getPublicCourses();
    }
}