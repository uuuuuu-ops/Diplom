package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Comment;
import com.diploma.Diplom.model.CommentTargetType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
            CommentTargetType targetType,
            String targetId
    );

    List<Comment> findByParentIdOrderByCreatedAtAsc(String parentId);
}