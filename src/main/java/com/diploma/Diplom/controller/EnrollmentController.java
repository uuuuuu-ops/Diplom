package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CourseAccessResponse;
import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Для бесплатного курса
    @PostMapping("/free/{courseId}")
    public Enrollment enrollFreeCourse(@PathVariable String courseId) {
        return enrollmentService.enrollFreeCourse(courseId);
    }

    // Проверка доступа к курсу
    @GetMapping("/check/{courseId}")
    public CourseAccessResponse checkAccess(@PathVariable String courseId) {
        String userId = enrollmentService.getCurrentUserId();
        boolean hasAccess = enrollmentService.hasAccess(userId, courseId);
        return new CourseAccessResponse(hasAccess);
    }

    // Мои enrollments
    @GetMapping("/my")
    public List<Enrollment> getMyEnrollments() {
        return enrollmentService.getMyEnrollments();
    }
}