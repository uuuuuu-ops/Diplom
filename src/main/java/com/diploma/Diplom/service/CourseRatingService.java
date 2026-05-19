package com.diploma.Diplom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.dto.RatingRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.model.CourseRating;
import com.diploma.Diplom.repository.CourseRatingRepository;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.CourseProgressRepository;

@Service
public class CourseRatingService {

    private final CourseRatingRepository ratingRepository;
    private final CourseRepository courseRepository;
    private final CourseProgressRepository progressRepository;

    public CourseRatingService(CourseRatingRepository ratingRepository,
                               CourseRepository courseRepository,
                               CourseProgressRepository progressRepository) {
        this.ratingRepository = ratingRepository;
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
    }

    
    public CourseRating rateOrUpdate(String userId, String courseId, RatingRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        CourseProgress progress = progressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ForbiddenException(
                        "You must be enrolled and have started this course to rate it"));

        if (progress.getCompletedLessonIds().isEmpty()) {
            throw new ForbiddenException(
                    "Complete at least one lesson before leaving a rating");
        }

        CourseRating rating = ratingRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> {
                    CourseRating r = new CourseRating();
                    r.setUserId(userId);
                    r.setCourseId(courseId);
                    r.setCreatedAt(LocalDateTime.now());
                    return r;
                });

        rating.setRating(request.getRating());
        rating.setReview(request.getReview());
        rating.setUpdatedAt(LocalDateTime.now());

        rating = ratingRepository.save(rating);
        recalcCourseAverage(courseId);
        return rating;
    }

    public List<CourseRating> getRatings(String courseId) {
        return ratingRepository.findByCourseId(courseId);
    }

    public void deleteRating(String userId, String courseId) {
        CourseRating rating = ratingRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found"));
        ratingRepository.delete(rating);
        recalcCourseAverage(courseId);
    }


    private void recalcCourseAverage(String courseId) {
        List<CourseRating> all = ratingRepository.findByCourseId(courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (all.isEmpty()) {
            course.setAvgRating(0.0);
            course.setRatingCount(0);
        } else {
            double avg = all.stream()
                    .mapToInt(CourseRating::getRating)
                    .average()
                    .orElse(0.0);
            course.setAvgRating(Math.round(avg * 10.0) / 10.0);
            course.setRatingCount(all.size());
        }
        courseRepository.save(course);
    }
}