package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CommentRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.LessonComment;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.LessonCommentRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonCommentService Tests")
class LessonCommentServiceTest {

    @Mock LessonCommentRepository commentRepository;
    @Mock LessonRepository lessonRepository;
    @Mock UserRepository userRepository;

    @InjectMocks
    LessonCommentService lessonCommentService;

    private Lesson lesson;
    private User student;
    private User teacher;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.setId("lesson-1");
        lesson.setCourseId("course-1");

        student = new User();
        student.setId("student-1");
        student.setRole(Role.STUDENT);

        teacher = new User();
        teacher.setId("teacher-1");
        teacher.setEmail("teacher@test.com");
        teacher.setRole(Role.TEACHER);
    }

    // ─────────────────────── addComment ──────────────────────────────────

    @Test
    @DisplayName("addComment: успешное добавление топ-уровневого комментария")
    void addComment_success() {
        CommentRequest req = new CommentRequest();
        req.setContent("Great lesson!");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LessonComment result = lessonCommentService.addComment("student-1", "lesson-1", req);

        assertThat(result.getContent()).isEqualTo("Great lesson!");
        assertThat(result.getAuthorId()).isEqualTo("student-1");
        assertThat(result.getLessonId()).isEqualTo("lesson-1");
        assertThat(result.getCourseId()).isEqualTo("course-1");
        assertThat(result.getParentId()).isNull();
    }

    @Test
    @DisplayName("addComment: пустой контент — BadRequestException")
    void addComment_emptyContent_throws() {
        CommentRequest req = new CommentRequest();
        req.setContent("   ");

        assertThatThrownBy(() -> lessonCommentService.addComment("student-1", "lesson-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    @DisplayName("addComment: null контент — BadRequestException")
    void addComment_nullContent_throws() {
        CommentRequest req = new CommentRequest();
        req.setContent(null);

        assertThatThrownBy(() -> lessonCommentService.addComment("student-1", "lesson-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("addComment: урок не найден — ResourceNotFoundException")
    void addComment_lessonNotFound_throws() {
        CommentRequest req = new CommentRequest();
        req.setContent("Hello");

        when(lessonRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonCommentService.addComment("student-1", "missing", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addComment: ответ на комментарий — сохраняется с parentId")
    void addComment_reply_success() {
        LessonComment parent = new LessonComment();
        parent.setId("comment-1");
        parent.setLessonId("lesson-1");
        parent.setParentId(null); // топ-уровень

        CommentRequest req = new CommentRequest();
        req.setContent("Reply text");
        req.setParentId("comment-1");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(parent));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LessonComment result = lessonCommentService.addComment("student-1", "lesson-1", req);

        assertThat(result.getParentId()).isEqualTo("comment-1");
    }

    @Test
    @DisplayName("addComment: ответ на ответ (вложенный) — BadRequestException")
    void addComment_replyToReply_throws() {
        LessonComment nestedParent = new LessonComment();
        nestedParent.setId("reply-1");
        nestedParent.setLessonId("lesson-1");
        nestedParent.setParentId("comment-1"); // уже ответ

        CommentRequest req = new CommentRequest();
        req.setContent("Nested reply");
        req.setParentId("reply-1");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(commentRepository.findById("reply-1")).thenReturn(Optional.of(nestedParent));

        assertThatThrownBy(() -> lessonCommentService.addComment("student-1", "lesson-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot reply to a reply");
    }

    @Test
    @DisplayName("addComment: parent принадлежит другому уроку — BadRequestException")
    void addComment_parentFromDifferentLesson_throws() {
        LessonComment parent = new LessonComment();
        parent.setId("comment-1");
        parent.setLessonId("other-lesson"); // другой урок
        parent.setParentId(null);

        CommentRequest req = new CommentRequest();
        req.setContent("Reply text");
        req.setParentId("comment-1");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> lessonCommentService.addComment("student-1", "lesson-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to this lesson");
    }

    // ─────────────────────── getComments ─────────────────────────────────

    @Test
    @DisplayName("getComments: возвращает топ-уровневые комментарии")
    void getComments_returnsTopLevel() {
        LessonComment c1 = new LessonComment();
        c1.setLessonId("lesson-1");
        c1.setContent("First comment");

        when(commentRepository.findByLessonIdAndParentIdIsNullOrderByCreatedAtAsc("lesson-1"))
                .thenReturn(List.of(c1));

        List<LessonComment> result = lessonCommentService.getComments("lesson-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("First comment");
    }

    // ─────────────────────── markAsAnswer ────────────────────────────────

    @Test
    @DisplayName("markAsAnswer: учитель помечает комментарий как ответ")
    void markAsAnswer_teacher_success() {
        LessonComment comment = new LessonComment();
        comment.setId("comment-1");
        comment.setMarkedAsAnswer(false);

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LessonComment result = lessonCommentService.markAsAnswer("teacher@test.com", "comment-1");

        assertThat(result.isMarkedAsAnswer()).isTrue();
    }

    @Test
    @DisplayName("markAsAnswer: студент не может пометить — ForbiddenException")
    void markAsAnswer_student_throws() {
        User s = new User();
        s.setId("student-1");
        s.setRole(Role.STUDENT);

        LessonComment comment = new LessonComment();
        comment.setId("comment-1");

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(s));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> lessonCommentService.markAsAnswer("student@test.com", "comment-1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only teachers");
    }

    // ─────────────────────── deleteComment ───────────────────────────────

    @Test
    @DisplayName("deleteComment: автор удаляет свой комментарий")
    void deleteComment_byAuthor_success() {
        LessonComment comment = new LessonComment();
        comment.setId("comment-1");
        comment.setAuthorId("student-1");
        comment.setParentId(null);

        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc("comment-1")).thenReturn(List.of());

        lessonCommentService.deleteComment("student-1", "comment-1");

        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("deleteComment: учитель удаляет чужой комментарий")
    void deleteComment_byTeacher_success() {
        LessonComment comment = new LessonComment();
        comment.setId("comment-1");
        comment.setAuthorId("student-1"); // не учитель
        comment.setParentId(null);

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc("comment-1")).thenReturn(List.of());

        lessonCommentService.deleteComment("teacher-1", "comment-1");

        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("deleteComment: студент удаляет чужой комментарий — ForbiddenException")
    void deleteComment_byOtherStudent_throws() {
        User otherStudent = new User();
        otherStudent.setId("other-student");
        otherStudent.setRole(Role.STUDENT);

        LessonComment comment = new LessonComment();
        comment.setId("comment-1");
        comment.setAuthorId("student-1");

        when(userRepository.findById("other-student")).thenReturn(Optional.of(otherStudent));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> lessonCommentService.deleteComment("other-student", "comment-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("deleteComment: удаление топ-уровневого комментария удаляет и все ответы")
    void deleteComment_withReplies_deletesReplies() {
        LessonComment parent = new LessonComment();
        parent.setId("comment-1");
        parent.setAuthorId("student-1");
        parent.setParentId(null);

        LessonComment reply = new LessonComment();
        reply.setId("reply-1");
        reply.setAuthorId("other-1");
        reply.setParentId("comment-1");

        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(parent));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc("comment-1")).thenReturn(List.of(reply));

        lessonCommentService.deleteComment("student-1", "comment-1");

        verify(commentRepository).deleteAll(List.of(reply));
        verify(commentRepository).delete(parent);
    }
}