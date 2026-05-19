package com.diploma.Diplom.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.Certificate;

public interface CertificateRepository extends MongoRepository<Certificate, String> {
    Optional<Certificate> findByVerificationCode(String verificationCode);
    Optional<Certificate> findByUserIdAndCourseId(String userId, String courseId);
}