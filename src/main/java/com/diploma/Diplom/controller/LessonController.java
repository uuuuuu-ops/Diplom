package com.diploma.Diplom.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.service.LessonService;

@RestController
@RequestMapping("/courses")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public Lesson createLesson(@PathVariable String courseId,
                               @RequestBody Lesson lesson) {
        return lessonService.createLesson(courseId, lesson);
    }

    @GetMapping("/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public List<Lesson> getLessonsByCourse(@PathVariable String courseId,
                                           @RequestParam String userId) {
        return lessonService.getLessonsByCourse(courseId, userId);
    }
}