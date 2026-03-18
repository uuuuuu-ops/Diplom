package com.diploma.Diplom.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.diploma.Diplom.model.Quiz;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    List<Quiz> findByLessonId(String lessonId);
}
