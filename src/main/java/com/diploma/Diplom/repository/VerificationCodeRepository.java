package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.VerificationCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByEmail(String email);
    Optional<VerificationCode> findByEmailAndCode(String email, String code);
}