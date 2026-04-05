package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.TeacherQuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TeacherQuizAttemptRepository extends MongoRepository<TeacherQuizAttempt, String> {
    Optional<TeacherQuizAttempt> findByUserId(String userId);
    Optional<TeacherQuizAttempt> findByApplicationId(String applicationId);
}