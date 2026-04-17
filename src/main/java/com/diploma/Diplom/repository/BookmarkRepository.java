package com.diploma.Diplom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.Bookmark;

public interface BookmarkRepository extends MongoRepository<Bookmark,String>{

    List<Bookmark> findByUserId(String userId); 

    Optional<Bookmark> findByUserIdAndCourseId(String userId,String courseId);

    void deleteByUserIdAndCourseId(String userId,String courseId);
}
