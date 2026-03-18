package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public Enrollment enrollStudent(Enrollment enrollment) {
        if (enrollmentRepository.findByUserIdAndCourseId(enrollment.getUserId(), enrollment.getCourseId()).isPresent()) {
            throw new RuntimeException("Student already enrolled in this course");
        }

        enrollment.setEnrolledAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getEnrollmentsByUser(String userId) {
        return enrollmentRepository.findByUserId(userId);
    }
}