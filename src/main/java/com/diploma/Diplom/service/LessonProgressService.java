package com.diploma.Diplom.service;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.repository.LessonRepository;

@Service
public class LessonProgressService {

    private final LessonRepository lessonRepository;
    private final CourseProgressService courseProgressService;

    public LessonProgressService(
            LessonRepository lessonRepository,
            CourseProgressService courseProgressService
    ) {
        this.lessonRepository = lessonRepository;
        this.courseProgressService = courseProgressService;
    }

    public void completeLesson(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        courseProgressService.markLessonCompleted(userId, lesson.getCourseId(), lessonId);
    }
    public String getCourseIdByLessonId(String lessonId) {
    Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

    return lesson.getCourseId();
}
}