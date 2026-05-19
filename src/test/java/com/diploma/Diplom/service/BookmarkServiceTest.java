package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Bookmark;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.repository.BookmarkRepository;
import com.diploma.Diplom.repository.CourseRepository;
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
@DisplayName("BookmarkService Tests")
class BookmarkServiceTest {

    @Mock BookmarkRepository bookmarkRepository;
    @Mock CourseRepository courseRepository;
    @Mock ActivityFeedService activityFeedService;

    @InjectMocks BookmarkService bookmarkService;

    @Test
    @DisplayName("toggleBookmark: не было закладки — создаёт, возвращает true")
    void toggleBookmark_noExisting_createsAndReturnsTrue() {
        when(bookmarkRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(bookmarkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = bookmarkService.toggleBookmark("user-1", "course-1");

        assertThat(result).isTrue();
        verify(bookmarkRepository).save(any(Bookmark.class));
        verify(activityFeedService).addActivity(eq("user-1"), any(), eq("course-1"), anyString());
    }

    @Test
    @DisplayName("toggleBookmark: закладка уже есть — удаляет, возвращает false")
    void toggleBookmark_existing_deletesAndReturnsFalse() {
        Bookmark existing = new Bookmark();
        when(bookmarkRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existing));

        boolean result = bookmarkService.toggleBookmark("user-1", "course-1");

        assertThat(result).isFalse();
        verify(bookmarkRepository).deleteByUserIdAndCourseId("user-1", "course-1");
        verify(bookmarkRepository, never()).save(any());
        verify(activityFeedService, never()).addActivity(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getUserBookmarks: есть закладки — возвращает курсы")
    void getUserBookmarks_withBookmarks_returnsCourses() {
        Bookmark b = new Bookmark();
        b.setCourseId("course-1");

        Course course = new Course();
        course.setId("course-1");
        course.setTitle("Java 101");

        when(bookmarkRepository.findByUserId("user-1")).thenReturn(List.of(b));
        when(courseRepository.findAllById(List.of("course-1"))).thenReturn(List.of(course));

        List<Course> result = bookmarkService.getUserBookmarks("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java 101");
    }

    @Test
    @DisplayName("getUserBookmarks: нет закладок — пустой список")
    void getUserBookmarks_noBookmarks_returnsEmpty() {
        when(bookmarkRepository.findByUserId("user-1")).thenReturn(List.of());

        List<Course> result = bookmarkService.getUserBookmarks("user-1");

        assertThat(result).isEmpty();
        verify(courseRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("isBookmarked: закладка существует — true")
    void isBookmarked_exists_returnsTrue() {
        when(bookmarkRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(new Bookmark()));

        assertThat(bookmarkService.isBookmarked("user-1", "course-1")).isTrue();
    }

    @Test
    @DisplayName("isBookmarked: закладки нет — false")
    void isBookmarked_notExists_returnsFalse() {
        when(bookmarkRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());

        assertThat(bookmarkService.isBookmarked("user-1", "course-1")).isFalse();
    }
}
