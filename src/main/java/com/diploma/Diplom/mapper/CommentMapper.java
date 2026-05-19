package com.diploma.Diplom.mapper;

import com.diploma.Diplom.dto.CommentResponse;
import com.diploma.Diplom.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment, String currentUserId) {

        CommentResponse dto = new CommentResponse();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());

        dto.setAuthorId(comment.getAuthorId());
        dto.setAuthorName(comment.getAuthorName());

        dto.setTargetId(comment.getTargetId());
        dto.setParentId(comment.getParentId());

        dto.setMarkedAsAnswer(comment.isMarkedAsAnswer());
        dto.setEdited(comment.isEdited());

        dto.setCreatedAt(comment.getCreatedAt());

        dto.setCanEdit(comment.getAuthorId().equals(currentUserId));

        return dto;
    }
}