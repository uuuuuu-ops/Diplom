package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Progress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProgressRepository extends MongoRepository<Progress, String> {
    Optional<Progress> findByUserIdAndCourseId(String userId, String courseId);
}