package com.diploma.Diplom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.model.Progress;
import com.diploma.Diplom.service.ProgressService;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public Progress completeLesson(@RequestParam String userId,
                                   @RequestParam String courseId,
                                   @RequestParam String lessonId) {
        return progressService.completeLesson(userId, courseId, lessonId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<Progress> getProgress(@RequestParam String userId,
                                                @RequestParam String courseId) {
        return progressService.getProgress(userId, courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}