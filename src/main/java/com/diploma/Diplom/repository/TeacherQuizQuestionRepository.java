package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.TeacherQuizQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TeacherQuizQuestionRepository extends MongoRepository<TeacherQuizQuestion, String> {
    List<TeacherQuizQuestion> findByTopic(String topic);
    List<TeacherQuizQuestion> findByTopicIgnoreCase(String topic);
}

// TeacherQuizAttemptRepository.java
package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.TeacherQuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TeacherQuizAttemptRepository extends MongoRepository<TeacherQuizAttempt, String> {
    Optional<TeacherQuizAttempt> findByUserId(String userId);
    Optional<TeacherQuizAttempt> findByApplicationId(String applicationId);
}