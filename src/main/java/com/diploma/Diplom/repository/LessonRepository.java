package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LessonRepository extends MongoRepository<Lesson, String> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(String courseId);
}