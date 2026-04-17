package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.BookmarkResponse;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.service.BookmarkService;
import com.diploma.Diplom.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Bookmark", description = "Bookmarks of Courses")
@RequestMapping("/courses")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final SecurityUtils securityUtils;

    public BookmarkController(BookmarkService bookmarkService,
                              SecurityUtils securityUtils) {
        this.bookmarkService = bookmarkService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/{courseId}/bookmark")
    public BookmarkResponse toggleBookmark(@PathVariable String courseId) {
        String userId = securityUtils.getCurrentUserId();
        boolean state = bookmarkService.toggleBookmark(userId, courseId);
        return new BookmarkResponse(state);
    }

    @GetMapping("/bookmarks")
    public List<Course> getBookmarks() {
        String userId = securityUtils.getCurrentUserId();
        return bookmarkService.getUserBookmarks(userId);
    }
}