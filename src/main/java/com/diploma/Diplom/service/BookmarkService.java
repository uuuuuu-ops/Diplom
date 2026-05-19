package com.diploma.Diplom.service;

import com.diploma.Diplom.model.ActivityType;
import com.diploma.Diplom.model.Bookmark;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.repository.BookmarkRepository;
import com.diploma.Diplom.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final CourseRepository courseRepository;
    private final ActivityFeedService activityFeedService;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           CourseRepository courseRepository,
                           ActivityFeedService activityFeedService) {
        this.bookmarkRepository = bookmarkRepository;
        this.courseRepository = courseRepository;
        this.activityFeedService = activityFeedService;
    }

    public boolean toggleBookmark(String userId, String courseId) {

    var existing = bookmarkRepository.findByUserIdAndCourseId(userId, courseId);

    if (existing.isPresent()) {
        bookmarkRepository.deleteByUserIdAndCourseId(userId, courseId);
        return false;
    }

    Bookmark bookmark = Bookmark.builder()
            .userId(userId)
            .courseId(courseId)
            .createdAt(LocalDateTime.now())
            .build();

    bookmarkRepository.save(bookmark);

    activityFeedService.addActivity(
            userId,
            ActivityType.BOOKMARK,
            courseId,
            "Added course to bookmarks ⭐"
    );

    return true;
}

    public List<Course> getUserBookmarks(String userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);

        List<String> courseIds = bookmarks.stream()
                .map(Bookmark::getCourseId)
                .toList();

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return courseRepository.findAllById(courseIds);
    }

    public boolean isBookmarked(String userId, String courseId) {
        return bookmarkRepository
                .findByUserIdAndCourseId(userId, courseId)
                .isPresent();
    }
}