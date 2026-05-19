package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.TeacherQuizQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TeacherQuizQuestionRepository extends MongoRepository<TeacherQuizQuestion, String> {
    List<TeacherQuizQuestion> findByTopic(String topic);
    List<TeacherQuizQuestion> findByTopicIgnoreCase(String topic);
}


