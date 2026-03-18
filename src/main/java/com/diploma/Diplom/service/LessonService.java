package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CourseRepository courseRepository;

    public LessonService(LessonRepository lessonRepository,
                         SubscriptionRepository subscriptionRepository,
                         CourseRepository courseRepository) {
        this.lessonRepository = lessonRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.courseRepository = courseRepository;
    }

    public Lesson createLesson(String courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        lesson.setCourseId(course.getId());
        
        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsByCourse(String courseId, String userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);

        if (subscriptions.isEmpty()) {
            throw new RuntimeException("You do not have a subscription");
        }

        Subscription subscription = subscriptions.get(0);

        if (!subscription.isActive()) {
            throw new RuntimeException("Your subscription is inactive");
        }

        if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Your subscription has expired");
        }

        return lessonRepository.findByCourseId(courseId);
    }

    public Optional<Lesson> getLessonById(String id) {
        return lessonRepository.findById(id);
    }

    public void deleteLesson(String id) {
        lessonRepository.deleteById(id);
    }
}