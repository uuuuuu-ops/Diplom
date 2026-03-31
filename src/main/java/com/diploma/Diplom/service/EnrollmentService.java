package com.diploma.Diplom.service;

import com.diploma.Diplom.model.AccessType;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.model.EnrollmentStatus;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.EnrollmentRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             SubscriptionService subscriptionService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    public String getCurrentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    public boolean hasAccess(String userId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Бесплатный курс доступен всем
        if (course.isFree()) {
            return true;
        }

        // Если курс куплен или выдан вручную
        boolean hasActiveEnrollment = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE);

        if (hasActiveEnrollment) {
            return true;
        }

        // Если есть активная подписка -> доступ ко всем курсам
        return subscriptionService.hasActiveSubscription(userId);
    }

    public Enrollment enrollFreeCourse(String courseId) {
        String userId = getCurrentUserId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.isFree()) {
            throw new RuntimeException("This course is not free");
        }

        Enrollment existing = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElse(null);

        if (existing != null) {
            return existing;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setAccessType(AccessType.FREE);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    public Enrollment activatePurchasedEnrollment(String userId, String courseId, String paymentId) {
        Enrollment existing = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElse(null);

        if (existing != null) {
            return existing;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setAccessType(AccessType.PURCHASE);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setPaymentId(paymentId);
        enrollment.setEnrolledAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    public Enrollment createManualEnrollment(String userId, String courseId) {
        Enrollment existing = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElse(null);

        if (existing != null) {
            return existing;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUserId(userId);
        enrollment.setCourseId(courseId);
        enrollment.setAccessType(AccessType.MANUAL);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getMyEnrollments() {
        return enrollmentRepository.findByUserId(getCurrentUserId());
    }

    public Enrollment getActiveEnrollment(String userId, String courseId) {
        return enrollmentRepository.findByUserIdAndCourseIdAndStatus(
                userId,
                courseId,
                EnrollmentStatus.ACTIVE
        ).orElse(null);
    }
}