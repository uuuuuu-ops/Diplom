package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Enrollment;
import com.diploma.Diplom.model.EnrollmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    Optional<Enrollment> findByUserIdAndCourseIdAndStatus(String userId, String courseId, EnrollmentStatus status);

    boolean existsByUserIdAndCourseIdAndStatus(String userId, String courseId, EnrollmentStatus status);

    List<Enrollment> findByUserId(String userId);
}