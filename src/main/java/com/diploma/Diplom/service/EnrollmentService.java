package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.messaging.EnrollmentProducer;
import com.diploma.Diplom.model.AccessType;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.model.EnrollmentStatus;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.EnrollmentRepository;
import com.diploma.Diplom.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SubscriptionService subscriptionService;
    private final EnrollmentProducer enrollmentProducer;
    private final SecurityUtils securityUtils;

    public String getCurrentUserId() {
        return securityUtils.getCurrentUserId();
    }

    /**
     * Кешируем на 5 минут. Ключ: userId:courseId.
     * Инвалидируем при любом изменении доступа.
     */
    @Cacheable(value = "access", key = "#userId + ':' + #courseId")
    public boolean hasAccess(String userId, String courseId) {
        log.debug("Cache miss — checking access userId={} courseId={}", userId, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return hasAccess(userId, course);
    }

    public boolean hasAccess(String userId, Course course) {
        if (course.isFree()) {
            return true;
        }
        boolean hasActiveEnrollment = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus(userId, course.getId(), EnrollmentStatus.ACTIVE);
        if (hasActiveEnrollment) {
            return true;
        }
        return subscriptionService.hasActiveSubscription(userId);
    }

    @CacheEvict(value = "access", key = "#result.userId + ':' + #result.courseId")
    public Enrollment enrollFreeCourse(String courseId) {
        String userId = getCurrentUserId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.isFree()) {
            throw new ForbiddenException("This course is not free");
        }

        boolean[] isNew = {false};
        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElseGet(() -> {
                    isNew[0] = true;
                    Enrollment e = new Enrollment();
                    e.setUserId(userId);
                    e.setCourseId(courseId);
                    e.setAccessType(AccessType.FREE);
                    e.setStatus(EnrollmentStatus.ACTIVE);
                    e.setEnrolledAt(LocalDateTime.now());
                    return enrollmentRepository.save(e);
                });

        // Async: welcome event only on first enrollment
        if (isNew[0]) {
            enrollmentProducer.sendEnrollmentEvent(userId, courseId, AccessType.FREE.name());
        }

        return enrollment;
    }

    @CacheEvict(value = "access", key = "#userId + ':' + #courseId")
    public Enrollment activatePurchasedEnrollment(String userId, String courseId, String paymentId) {
        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUserId(userId);
                    e.setCourseId(courseId);
                    e.setAccessType(AccessType.PURCHASE);
                    e.setStatus(EnrollmentStatus.ACTIVE);
                    e.setPaymentId(paymentId);
                    e.setEnrolledAt(LocalDateTime.now());
                    return enrollmentRepository.save(e);
                });

        // Async: welcome event
        enrollmentProducer.sendEnrollmentEvent(userId, courseId, AccessType.PURCHASE.name());

        return enrollment;
    }

    @CacheEvict(value = "access", key = "#userId + ':' + #courseId")
    public Enrollment createManualEnrollment(String userId, String courseId) {
        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setUserId(userId);
                    e.setCourseId(courseId);
                    e.setAccessType(AccessType.MANUAL);
                    e.setStatus(EnrollmentStatus.ACTIVE);
                    e.setEnrolledAt(LocalDateTime.now());
                    return enrollmentRepository.save(e);
                });

        enrollmentProducer.sendEnrollmentEvent(userId, courseId, AccessType.MANUAL.name());

        return enrollment;
    }

    public List<Enrollment> getMyEnrollments() {
        return enrollmentRepository.findByUserId(getCurrentUserId());
    }

    public Enrollment getActiveEnrollment(String userId, String courseId) {
        return enrollmentRepository.findByUserIdAndCourseIdAndStatus(
                userId, courseId, EnrollmentStatus.ACTIVE
        ).orElse(null);
    }
}