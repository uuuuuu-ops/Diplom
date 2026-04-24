package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CommentRequest;
import com.diploma.Diplom.dto.CommentResponse;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.messaging.CommentNotificationProducer;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.CommentRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock UserRepository userRepository;
    @Mock CommentNotificationProducer notificationProducer;

    @InjectMocks
    CommentService commentService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setName("Alice");
    }

    @Test
    @DisplayName("addComment: успешное создание комментария к курсу")
    void addComment_success() {
        CommentRequest req = new CommentRequest();
        req.setContent("Great course!");

        Comment saved = new Comment();
        saved.setId("comment-1");
        saved.setContent("Great course!");
        saved.setAuthorId("user-1");
        saved.setAuthorName("Alice");
        saved.setTargetId("course-1");
        saved.setTargetType(CommentTargetType.COURSE);
        saved.setCreatedAt(Instant.now());

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenReturn(saved);

        CommentResponse result = commentService.addComment(
                "user-1", CommentTargetType.COURSE, "course-1", req);

        assertThat(result.getContent()).isEqualTo("Great course!");
        assertThat(result.getAuthorName()).isEqualTo("Alice");
        assertThat(result.isCanEdit()).isTrue();
        verify(notificationProducer).notifyNewComment(
                eq(CommentTargetType.COURSE), eq("course-1"),
                eq("user-1"), eq("Alice"), anyString());
    }

    @Test
    @DisplayName("addComment: пустой текст — BadRequestException")
    void addComment_blankContent_throws() {
        CommentRequest req = new CommentRequest();
        req.setContent("   ");

        assertThatThrownBy(() ->
                commentService.addComment("user-1", CommentTargetType.COURSE, "course-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
        verifyNoInteractions(commentRepository, notificationProducer);
    }

    @Test
    @DisplayName("addComment: null контент — BadRequestException")
    void addComment_nullContent_throws() {
        CommentRequest req = new CommentRequest();
        req.setContent(null);

        assertThatThrownBy(() ->
                commentService.addComment("user-1", CommentTargetType.COURSE, "course-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("addComment: пользователь не найден — RuntimeException")
    void addComment_userNotFound_throws() {
        CommentRequest req = new CommentRequest();
        req.setContent("Hello");

        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.addComment("ghost", CommentTargetType.COURSE, "course-1", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("addComment: ответ на другой комментарий — parentId сохраняется")
    void addComment_reply_setsParentId() {
        CommentRequest req = new CommentRequest();
        req.setContent("I agree!");
        req.setParentId("parent-comment-1");

        Comment saved = new Comment();
        saved.setId("comment-2");
        saved.setContent("I agree!");
        saved.setAuthorId("user-1");
        saved.setAuthorName("Alice");
        saved.setTargetId("lesson-1");
        saved.setTargetType(CommentTargetType.LESSON);
        saved.setParentId("parent-comment-1");
        saved.setCreatedAt(Instant.now());

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(commentRepository.save(any())).thenReturn(saved);

        CommentResponse result = commentService.addComment(
                "user-1", CommentTargetType.LESSON, "lesson-1", req);

        assertThat(result.getParentId()).isEqualTo("parent-comment-1");
    }

    @Test
    @DisplayName("getComments: возвращает корневые комментарии с вложенными ответами")
    void getComments_returnsTree() {
        Comment root = new Comment();
        root.setId("root-1");
        root.setContent("Root comment");
        root.setAuthorId("user-1");
        root.setAuthorName("Alice");
        root.setTargetId("course-1");
        root.setTargetType(CommentTargetType.COURSE);
        root.setCreatedAt(Instant.now());

        Comment reply = new Comment();
        reply.setId("reply-1");
        reply.setContent("Reply");
        reply.setAuthorId("user-2");
        reply.setAuthorName("Bob");
        reply.setParentId("root-1");
        reply.setCreatedAt(Instant.now());

        when(commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
                CommentTargetType.COURSE, "course-1"))
                .thenReturn(List.of(root));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc("root-1"))
                .thenReturn(List.of(reply));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc("reply-1"))
                .thenReturn(List.of());

        List<CommentResponse> result = commentService.getComments(
                CommentTargetType.COURSE, "course-1", "user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Root comment");
        assertThat(result.get(0).getReplies()).hasSize(1);
        assertThat(result.get(0).getReplies().get(0).getContent()).isEqualTo("Reply");
    }

    @Test
    @DisplayName("getComments: нет комментариев — пустой список")
    void getComments_empty_returnsList() {
        when(commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
                CommentTargetType.COURSE, "course-1"))
                .thenReturn(List.of());

        List<CommentResponse> result = commentService.getComments(
                CommentTargetType.COURSE, "course-1", "user-1");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getComments: canEdit=true только для своих комментариев")
    void getComments_canEdit_onlyForOwner() {
        Comment c1 = new Comment();
        c1.setId("c1");
        c1.setContent("My comment");
        c1.setAuthorId("user-1");
        c1.setAuthorName("Alice");
        c1.setTargetId("course-1");
        c1.setTargetType(CommentTargetType.COURSE);
        c1.setCreatedAt(Instant.now());

        Comment c2 = new Comment();
        c2.setId("c2");
        c2.setContent("Other comment");
        c2.setAuthorId("user-2");
        c2.setAuthorName("Bob");
        c2.setTargetId("course-1");
        c2.setTargetType(CommentTargetType.COURSE);
        c2.setCreatedAt(Instant.now());

        when(commentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtDesc(
                CommentTargetType.COURSE, "course-1"))
                .thenReturn(List.of(c1, c2));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        List<CommentResponse> result = commentService.getComments(
                CommentTargetType.COURSE, "course-1", "user-1");

        assertThat(result.get(0).isCanEdit()).isTrue();
        assertThat(result.get(1).isCanEdit()).isFalse();
    }
}