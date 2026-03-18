package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Certificate;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Progress;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final CertificateRepository certificateRepository;

    public ProgressService(ProgressRepository progressRepository,
                           LessonRepository lessonRepository,
                           CertificateRepository certificateRepository) {
        this.progressRepository = progressRepository;
        this.lessonRepository = lessonRepository;
        this.certificateRepository = certificateRepository;
    }

    public Progress completeLesson(String userId, String courseId, String lessonId) {
        Progress progress = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> {
                    Progress newProgress = new Progress();
                    newProgress.setUserId(userId);
                    newProgress.setCourseId(courseId);
                    return newProgress;
                });

        if (!progress.getCompletedLessons().contains(lessonId)) {
            progress.getCompletedLessons().add(lessonId);
        }

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        int totalLessons = lessons.size();
        int completed = progress.getCompletedLessons().size();

        int percent = 0;
        if (totalLessons > 0) {
            percent = (completed * 100) / totalLessons;
        }

        progress.setProgressPercent(percent);
        Progress saved = progressRepository.save(progress);

        if (percent == 100) {
            boolean exists = certificateRepository.findByUserId(userId)
                    .stream()
                    .anyMatch(c -> c.getCourseId().equals(courseId));

            if (!exists) {
                Certificate certificate = new Certificate();
                certificate.setUserId(userId);
                certificate.setCourseId(courseId);
                certificate.setIssuedAt(LocalDateTime.now());
                certificate.setCertificateUrl("generated-later");
                certificateRepository.save(certificate);
            }
        }

        return saved;
    }

    public Optional<Progress> getProgress(String userId, String courseId) {
        return progressRepository.findByUserIdAndCourseId(userId, courseId);
    }
}