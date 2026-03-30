package com.diploma.Diplom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.Quiz;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    Optional<Quiz> findByLessonId(String lessonId);
    List<Quiz> findByLessonIdIn(List<String> lessonIds);
}