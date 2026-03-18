package com.diploma.Diplom.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.Lesson;

public interface LessonRepository extends MongoRepository<Lesson, String> {

    List<Lesson> findByCourseId(String courseId);

}