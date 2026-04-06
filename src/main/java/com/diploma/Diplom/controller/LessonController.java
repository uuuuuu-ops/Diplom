package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateLessonRequest;
import com.diploma.Diplom.dto.UpdateLessonRequest;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.service.LessonService;

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

import java.util.List;

@RestController
@RequestMapping("/lessons")
@Tag(name = "Lessons", description = "Add, update, and retrieve lessons inside a course")
@SecurityRequirement(name = "bearerAuth")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @Operation(
        summary = "Add a lesson to a course (TEACHER)",
        description = """
            Multipart form. Upload a video file and/or a PDF lecture.
            Set `quizRequired=true` to force students to pass the quiz before
            this lesson can be marked complete.
            """,
        responses = {
            @ApiResponse(responseCode = "200", description = "Lesson created",
                content = @Content(schema = @Schema(implementation = Lesson.class))),
            @ApiResponse(responseCode = "403", description = "Not course owner", content = @Content)
        }
    )
    @PostMapping(value = "/course/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Lesson addLessonToCourse(
            Authentication authentication,
            @PathVariable String courseId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @Parameter(description = "Order in the course, starting from 0")
            @RequestParam int orderIndex,
            @Parameter(description = "Estimated duration in minutes")
            @RequestParam int duration,
            @RequestParam(required = false) String lectureText,
            @Parameter(description = "If true, student must pass quiz before completing lesson")
            @RequestParam(required = false, defaultValue = "false") boolean quizRequired,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile lecturePdfFile
    ) {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setOrderIndex(orderIndex);
        request.setDuration(duration);
        request.setLectureText(lectureText);
        request.setQuizRequired(quizRequired);
        return lessonService.addLessonToCourse(authentication.getName(), courseId, request,
                videoFile, lecturePdfFile);
    }

    @Operation(
        summary = "Get all lessons for a course",
        description = "Ordered by orderIndex ascending.",
        responses = @ApiResponse(responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Lesson.class))))
    )
    @GetMapping("/course/{courseId}")
    public List<Lesson> getLessonsByCourseId(@PathVariable String courseId) {
        return lessonService.getLessonsByCourseId(courseId);
    }

    @Operation(
        summary = "Get a single lesson by ID",
        responses = {
            @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = Lesson.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
        }
    )
    @GetMapping("/{lessonId}")
    public Lesson getLessonById(@PathVariable String lessonId) {
        return lessonService.getLessonById(lessonId);
    }

    @Operation(
        summary = "Update a lesson (TEACHER)",
        description = "All fields optional. Pass `quizRequired` to toggle the quiz gate.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Updated",
                content = @Content(schema = @Schema(implementation = Lesson.class))),
            @ApiResponse(responseCode = "403", description = "Not course owner", content = @Content)
        }
    )
    @PutMapping(value = "/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Lesson updateLesson(
            Authentication authentication,
            @PathVariable String lessonId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer orderIndex,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) String lectureText,
            @RequestParam(required = false) Boolean published,
            @Parameter(description = "Toggle quiz gate. null = no change")
            @RequestParam(required = false) Boolean quizRequired,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile lecturePdfFile
    ) {
        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setOrderIndex(orderIndex);
        request.setDuration(duration);
        request.setLectureText(lectureText);
        request.setPublished(published);
        request.setQuizRequired(quizRequired);
        return lessonService.updateLesson(authentication.getName(), lessonId, request,
                videoFile, lecturePdfFile);
    }

    @Operation(
        summary = "Delete a lesson (TEACHER)",
        description = "Also deletes the video and PDF from Cloudinary.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Not course owner", content = @Content)
        }
    )
    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteLesson(Authentication authentication, @PathVariable String lessonId) {
        lessonService.deleteLesson(authentication.getName(), lessonId);
        return "Lesson deleted successfully";
    }
}