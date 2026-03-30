package com.diploma.Diplom.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.CompleteLessonRequest;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.CourseProgressService;
import com.diploma.Diplom.service.LessonProgressService;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final CourseProgressService courseProgressService;
    private final LessonProgressService lessonProgressService;
    private final UserRepository userRepository;

    public ProgressController(
            CourseProgressService courseProgressService,
            LessonProgressService lessonProgressService,
            UserRepository userRepository
    ) {
        this.courseProgressService = courseProgressService;
        this.lessonProgressService = lessonProgressService;
        this.userRepository = userRepository;
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CourseProgress> completeLesson(
            @RequestBody CompleteLessonRequest request,
            Principal principal
    ) {
        User user = getCurrentUser(principal);

        lessonProgressService.completeLesson(user.getId(), request.getLessonId());

        courseProgressService.markLessonCompleted(
                user.getId(),
                request.getCourseId(),
                request.getLessonId()
        );

        return ResponseEntity.ok(
                courseProgressService.getProgress(user.getId(), request.getCourseId())
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','ADMIN')")
    public ResponseEntity<CourseProgress> getProgress(
            @RequestParam String courseId,
            Principal principal
    ) {
        String userId = getCurrentUser(principal).getId();
        return ResponseEntity.ok(courseProgressService.getProgress(userId, courseId));
    }

    private User getCurrentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}