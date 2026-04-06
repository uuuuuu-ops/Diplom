package com.diploma.Diplom.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.diploma.Diplom.model.LessonComment;

public interface LessonCommentRepository extends MongoRepository<LessonComment, String> {

    List<LessonComment> findByLessonIdAndParentIdIsNullOrderByCreatedAtAsc(String lessonId);

    List<LessonComment> findByParentIdOrderByCreatedAtAsc(String parentId);
}