package com.diploma.Diplom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.TeacherApplication;

public interface TeacherApplicationRepository extends MongoRepository<TeacherApplication,String>{
    Optional<TeacherApplication> findByUserId(String userId);
    List<TeacherApplication> findByStatus (String status);
    
}
