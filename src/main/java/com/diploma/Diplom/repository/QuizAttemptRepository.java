package com.diploma.Diplom.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.QuizAttempt;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {
    List<QuizAttempt> findByUserIdAndQuizId(String userId, String quizId);
}