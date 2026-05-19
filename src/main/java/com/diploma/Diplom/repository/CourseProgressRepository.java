package com.diploma.Diplom.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.CourseProgress;

public interface CourseProgressRepository extends MongoRepository<CourseProgress, String> {
    Optional<CourseProgress> findByUserIdAndCourseId(String userId, String courseId);
}