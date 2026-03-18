package com.diploma.Diplom.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.diploma.Diplom.model.Quiz;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    Optional<Quiz> findByLessonId(String lessonId);
}
