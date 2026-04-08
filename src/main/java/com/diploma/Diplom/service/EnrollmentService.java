package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.AccessType;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.model.EnrollmentStatus;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.EnrollmentRepository;
import com.diploma.Diplom.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;

    public String getCurrentUserId() {
        return securityUtils.getCurrentUserId();
    }

    public boolean hasAccess(String userId, String courseId) {
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

    public Enrollment enrollFreeCourse(String courseId) {
        String userId = getCurrentUserId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.isFree()) {
            throw new ForbiddenException("This course is not free");
        }

        return enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElseGet(() -> {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUserId(userId);
                    enrollment.setCourseId(courseId);
                    enrollment.setAccessType(AccessType.FREE);
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollment.setEnrolledAt(LocalDateTime.now());
                    return enrollmentRepository.save(enrollment);
                });
    }

    public Enrollment activatePurchasedEnrollment(String userId, String courseId, String paymentId) {
        return enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElseGet(() -> {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUserId(userId);
                    enrollment.setCourseId(courseId);
                    enrollment.setAccessType(AccessType.PURCHASE);
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollment.setPaymentId(paymentId);
                    enrollment.setEnrolledAt(LocalDateTime.now());
                    return enrollmentRepository.save(enrollment);
                });
    }

    public Enrollment createManualEnrollment(String userId, String courseId) {
        return enrollmentRepository
                .findByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ACTIVE)
                .orElseGet(() -> {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUserId(userId);
                    enrollment.setCourseId(courseId);
                    enrollment.setAccessType(AccessType.MANUAL);
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    enrollment.setEnrolledAt(LocalDateTime.now());
                    return enrollmentRepository.save(enrollment);
                });
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
