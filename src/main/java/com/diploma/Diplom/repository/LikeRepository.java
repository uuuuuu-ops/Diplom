package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    Optional<Like> findByUserIdAndCourseId(String userId, String courseId);
    long countByCourseId(String courseId);
    void deleteByUserIdAndCourseId(String userId, String courseId);
}