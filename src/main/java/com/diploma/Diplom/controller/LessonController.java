package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateLessonRequest;
import com.diploma.Diplom.dto.UpdateLessonRequest;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.service.LessonService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping(value = "/course/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Lesson addLessonToCourse(Authentication authentication,
                                    @PathVariable String courseId,
                                    @RequestParam String title,
                                    @RequestParam(required = false) String description,
                                    @RequestParam int orderIndex,
                                    @RequestParam int duration,
                                    @RequestParam(required = false) String lectureText,
                                    @RequestParam(required = false) MultipartFile videoFile,
                                    @RequestParam(required = false) MultipartFile lecturePdfFile) {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setOrderIndex(orderIndex);
        request.setDuration(duration);
        request.setLectureText(lectureText);

        return lessonService.addLessonToCourse(
                authentication.getName(),
                courseId,
                request,
                videoFile,
                lecturePdfFile
        );
    }

    @GetMapping("/course/{courseId}")
    public List<Lesson> getLessonsByCourseId(@PathVariable String courseId) {
        return lessonService.getLessonsByCourseId(courseId);
    }

    @GetMapping("/{lessonId}")
    public Lesson getLessonById(@PathVariable String lessonId) {
        return lessonService.getLessonById(lessonId);
    }

    @PutMapping(value = "/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Lesson updateLesson(Authentication authentication,
                               @PathVariable String lessonId,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) Integer orderIndex,
                               @RequestParam(required = false) Integer duration,
                               @RequestParam(required = false) String lectureText,
                               @RequestParam(required = false) Boolean published,
                               @RequestParam(required = false) MultipartFile videoFile,
                               @RequestParam(required = false) MultipartFile lecturePdfFile) {
        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setOrderIndex(orderIndex);
        request.setDuration(duration);
        request.setLectureText(lectureText);
        request.setPublished(published);

        return lessonService.updateLesson(
                authentication.getName(),
                lessonId,
                request,
                videoFile,
                lecturePdfFile
        );
    }

    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteLesson(Authentication authentication,
                               @PathVariable String lessonId) {
        lessonService.deleteLesson(authentication.getName(), lessonId);
        return "Lesson deleted successfully";
    }
}