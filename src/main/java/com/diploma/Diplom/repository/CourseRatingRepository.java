package com.diploma.Diplom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.CourseRating;

public interface CourseRatingRepository extends MongoRepository<CourseRating, String> {

    Optional<CourseRating> findByUserIdAndCourseId(String userId, String courseId);

    List<CourseRating> findByCourseId(String courseId);

    long countByCourseId(String courseId);
}