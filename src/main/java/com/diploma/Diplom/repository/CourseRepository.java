package com.diploma.Diplom.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.diploma.Diplom.model.Course;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findByTeacherId(String teacherId);

}