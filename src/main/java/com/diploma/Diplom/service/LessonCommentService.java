package com.diploma.Diplom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.dto.CommentRequest;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.LessonComment;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.LessonCommentRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.UserRepository;

@Service
public class LessonCommentService {

    private final LessonCommentRepository commentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public LessonCommentService(LessonCommentRepository commentRepository,
                                LessonRepository lessonRepository,
                                UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    public LessonComment addComment(String authorId, String lessonId, CommentRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("Comment content cannot be empty");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (request.getParentId() != null) {
            LessonComment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            if (!parent.getLessonId().equals(lessonId)) {
                throw new RuntimeException("Parent comment does not belong to this lesson");
            }
            // no nested replies — only one level of threading
            if (parent.getParentId() != null) {
                throw new RuntimeException("Cannot reply to a reply");
            }
        }

        LessonComment comment = new LessonComment();
        comment.setLessonId(lessonId);
        comment.setCourseId(lesson.getCourseId());
        comment.setAuthorId(authorId);
        comment.setContent(request.getContent().trim());
        comment.setParentId(request.getParentId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    /** Top-level comments for a lesson, each with its replies attached. */
    public List<LessonComment> getComments(String lessonId) {
        return commentRepository
                .findByLessonIdAndParentIdIsNullOrderByCreatedAtAsc(lessonId);
    }

    public List<LessonComment> getReplies(String parentId) {
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
    }
    public LessonComment markAsAnswer(String teacherEmail, String commentId) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LessonComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (teacher.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only teachers can mark answers");
        }

        comment.setMarkedAsAnswer(true);
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }


    public void deleteComment(String userId, String commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LessonComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isAuthor = comment.getAuthorId().equals(userId);
        boolean isTeacher = user.getRole() == Role.TEACHER;

        if (!isAuthor && !isTeacher) {
            throw new RuntimeException("You do not have permission to delete this comment");
        }

        if (comment.getParentId() == null) {
            List<LessonComment> replies = commentRepository
                    .findByParentIdOrderByCreatedAtAsc(commentId);
            commentRepository.deleteAll(replies);
        }

        commentRepository.delete(comment);
    }
}