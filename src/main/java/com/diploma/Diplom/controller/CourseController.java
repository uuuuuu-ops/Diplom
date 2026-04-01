package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateCourseRequest;
import com.diploma.Diplom.dto.UpdateCourseRequest;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.service.CourseService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Course createCourse(Authentication authentication,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam String category,
                               @RequestParam(required = false) String level,
                               @RequestParam(required = false) MultipartFile thumbnailFile,
                               @RequestParam(required = false) Boolean free,
                               @RequestParam(required = false) BigDecimal price) {
        

        System.out.println("price = " + price);


        CreateCourseRequest request = new CreateCourseRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setLevel(level);
        request.setFree(free);
        request.setPrice(price);
        System.out.println("CONTROLLER request.getPrice() = " + request.getPrice());


        return courseService.createCourse(authentication.getName(), request, thumbnailFile);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public List<Course> getMyCourses(Authentication authentication) {
        return courseService.getTeacherCourses(authentication.getName());
    }

    @GetMapping("/{courseId}")
    public Course getCourseById(@PathVariable String courseId) {
        return courseService.getCourseById(courseId);
    }

    @PutMapping(value = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public Course updateCourse(Authentication authentication,
                               @PathVariable String courseId,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String level,
                               @RequestParam(required = false) Boolean published,
                               @RequestParam(required = false) MultipartFile thumbnailFile) {
        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setLevel(level);
        request.setPublished(published);

        return courseService.updateCourse(authentication.getName(), courseId, request, thumbnailFile);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteCourse(Authentication authentication,
                               @PathVariable String courseId) {
        courseService.deleteCourse(authentication.getName(), courseId);
        return "Course deleted successfully";
    }

    @GetMapping("/public")
    public List<Course> getPublicCourses() {
        return courseService.getPublicCourses();
    }
}