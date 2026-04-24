package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CommentRequest;
import com.diploma.Diplom.dto.CommentResponse;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.CommentRepository;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.messaging.CommentNotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentNotificationProducer notificationProducer;

    public CommentResponse addComment(String userId,
                                      CommentTargetType type,
                                      String targetId,
                                      CommentRequest request) {

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Comment cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent().trim());

        comment.setAuthorId(userId);
        comment.setAuthorName(user.getName());

        comment.setTargetId(targetId);
        comment.setTargetType(type);

        comment.setParentId(request.getParentId());

        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        Comment saved = commentRepository.save(comment);

        notificationProducer.notifyNewComment(
                type,
                targetId,
                userId,
                user.getName(),
                comment.getContent()
        );

        return toResponse(saved, userId);
    }

    public List<CommentResponse> getComments(CommentTargetType type,
                                            String targetId,
                                            String userId) {

        List<Comment> roots =
                commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
                        type, targetId
                );

        return roots.stream()
                .map(c -> buildTree(c, userId))
                .toList();
    }

    private CommentResponse buildTree(Comment comment, String userId) {

        CommentResponse res = toResponse(comment, userId);

        List<CommentResponse> replies =
                commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId())
                        .stream()
                        .map(c -> buildTree(c, userId))
                        .toList();

        res.setReplies(replies);

        return res;
    }

    
    private CommentResponse toResponse(Comment c, String userId) {

        CommentResponse r = new CommentResponse();

        r.setId(c.getId());
        r.setContent(c.getContent());

        r.setAuthorId(c.getAuthorId());
        r.setAuthorName(c.getAuthorName());

        r.setTargetId(c.getTargetId());
        r.setParentId(c.getParentId());

        r.setMarkedAsAnswer(c.isMarkedAsAnswer());
        r.setEdited(c.isEdited());

        r.setCreatedAt(c.getCreatedAt());

        r.setCanEdit(c.getAuthorId().equals(userId));

        return r;
    }
}