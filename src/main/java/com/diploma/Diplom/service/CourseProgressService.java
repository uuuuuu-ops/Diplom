package com.diploma.Diplom.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.messaging.ActivityProducer;
import com.diploma.Diplom.messaging.CertificateProducer;
import com.diploma.Diplom.model.ActivityType;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.repository.CourseProgressRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.QuizRepository;

@Service
public class CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateProducer certificateProducer;
    private final ActivityProducer activityProducer;

    public CourseProgressService(
            CourseProgressRepository courseProgressRepository,
            LessonRepository lessonRepository,
            QuizRepository quizRepository,
            CertificateRepository certificateRepository,
            CertificateProducer certificateProducer,
            ActivityProducer activityProducer
    ) {
        this.courseProgressRepository = courseProgressRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.certificateRepository = certificateRepository;
        this.certificateProducer = certificateProducer;
        this.activityProducer = activityProducer;
    }

    @CacheEvict(value = "progress", key = "#userId + ':' + #courseId")
    public CourseProgress markLessonCompleted(String userId, String courseId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (lesson.isQuizRequired()) {
            Quiz quiz = quizRepository.findByLessonId(lessonId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "This lesson requires a quiz but no quiz was found. Contact your teacher."));

            CourseProgress progress = getOrCreateProgress(userId, courseId);
            if (!progress.getPassedQuizIds().contains(quiz.getId())) {
                throw new ForbiddenException(
                        "You must pass this lesson's quiz before marking it complete.");
            }
        }

        CourseProgress progress = getOrCreateProgress(userId, courseId);
        progress.getCompletedLessonIds().add(lessonId);
        progress.setLastUpdatedAt(LocalDateTime.now());

        recalculateProgress(progress);
        CourseProgress saved = courseProgressRepository.save(progress);

        // Async activity via RabbitMQ — не блокирует HTTP-ответ
        activityProducer.sendActivity(
                userId,
                ActivityType.LESSON_COMPLETED.name(),
                lessonId,
                "Completed lesson: " + lesson.getTitle()
        );

        return saved;
    }

    @CacheEvict(value = "progress", key = "#userId + ':' + #courseId")
    public CourseProgress markQuizPassed(String userId, String courseId, String quizId) {
        CourseProgress progress = getOrCreateProgress(userId, courseId);
        progress.getPassedQuizIds().add(quizId);
        progress.setLastUpdatedAt(LocalDateTime.now());

        recalculateProgress(progress);
        return courseProgressRepository.save(progress);
    }

    public boolean isLessonUnlocked(String userId, String courseId, String lessonId) {
        Lesson target = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (target.getOrderIndex() == 0) return true;

        List<Lesson> allLessons = lessonRepository
                .findByCourseIdOrderByOrderIndexAsc(courseId);

        CourseProgress progress = getOrCreateProgress(userId, courseId);

        for (Lesson previous : allLessons) {
            if (!previous.isPublished()) continue;
            if (previous.getOrderIndex() >= target.getOrderIndex()) break;

            if (!progress.getCompletedLessonIds().contains(previous.getId())) {
                return false;
            }

            if (previous.isQuizRequired()) {
                Quiz quiz = quizRepository.findByLessonId(previous.getId()).orElse(null);
                if (quiz != null && !progress.getPassedQuizIds().contains(quiz.getId())) {
                    return false;
                }
            }
        }

        return true;
    }

    public CourseProgress getProgressByLessonId(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        return getProgress(userId, lesson.getCourseId());
    }

    @Cacheable(value = "progress", key = "#userId + ':' + #courseId")
    public CourseProgress getProgress(String userId, String courseId) {
        return getOrCreateProgress(userId, courseId);
    }

    private CourseProgress getOrCreateProgress(String userId, String courseId) {
        return courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> {
                    CourseProgress progress = new CourseProgress();
                    progress.setUserId(userId);
                    progress.setCourseId(courseId);
                    progress.setProgressPercent(0);
                    progress.setCompleted(false);
                    progress.setLastUpdatedAt(LocalDateTime.now());
                    return courseProgressRepository.save(progress);
                });
    }

    private void recalculateProgress(CourseProgress progress) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(progress.getCourseId());

        Set<String> lessonIds = lessons.stream()
                .map(Lesson::getId)
                .collect(Collectors.toSet());

        List<Quiz> quizzes = lessonIds.isEmpty()
                ? List.of()
                : quizRepository.findByLessonIdIn(List.copyOf(lessonIds));

        int totalLessons = lessons.size();
        int totalQuizzes = quizzes.size();

        int completedLessons = (int) lessons.stream()
                .filter(lesson -> progress.getCompletedLessonIds().contains(lesson.getId()))
                .count();

        int passedQuizzes = (int) quizzes.stream()
                .filter(quiz -> progress.getPassedQuizIds().contains(quiz.getId()))
                .count();

        int totalItems = totalLessons + totalQuizzes;
        int completedItems = completedLessons + passedQuizzes;

        int progressPercent = totalItems == 0 ? 0 : (completedItems * 100) / totalItems;
        progress.setProgressPercent(progressPercent);

        boolean allLessonsCompleted = totalLessons == 0 || completedLessons == totalLessons;
        boolean allQuizzesPassed = totalQuizzes == 0 || passedQuizzes == totalQuizzes;

        boolean courseCompleted = allLessonsCompleted && allQuizzesPassed;
        progress.setCompleted(courseCompleted);

        if (courseCompleted && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());

            boolean certificateExists = certificateRepository
                    .findByUserIdAndCourseId(progress.getUserId(), progress.getCourseId())
                    .isPresent();

            if (!certificateExists) {
                certificateProducer.requestCertificate(progress.getUserId(), progress.getCourseId());
            }
        }
    }
}
