package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByTeacherId(String teacherId);

    
    List<Course> findByPublishedTrue();

    Page<Course> findByPublishedTrue(Pageable pageable);

    Page<Course> findByPublishedTrueAndCategory(String category, Pageable pageable);

    Page<Course> findByPublishedTrueAndLevel(String level, Pageable pageable);

    Page<Course> findByPublishedTrueAndCategoryAndLevel(String category, String level, Pageable pageable);
}