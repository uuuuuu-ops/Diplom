package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.RatingRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.model.CourseRating;
import com.diploma.Diplom.repository.CourseProgressRepository;
import com.diploma.Diplom.repository.CourseRatingRepository;
import com.diploma.Diplom.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("CourseRatingService Tests")
class CourseRatingServiceTest {

    @Mock CourseRatingRepository ratingRepository;
    @Mock CourseRepository courseRepository;
    @Mock CourseProgressRepository progressRepository;

    @InjectMocks
    CourseRatingService courseRatingService;

    private CourseProgress progressWithLesson;
    private Course course;

    @BeforeEach
    void setUp() {
        progressWithLesson = new CourseProgress();
        progressWithLesson.setUserId("user-1");
        progressWithLesson.setCourseId("course-1");
        Set<String> completed = new HashSet<>();
        completed.add("lesson-1");
        progressWithLesson.setCompletedLessonIds(completed);
        progressWithLesson.setPassedQuizIds(new HashSet<>());

        course = new Course();
        course.setId("course-1");
        course.setAvgRating(0.0);
        course.setRatingCount(0);
    }


    @Test
    @DisplayName("rateOrUpdate: успешная оценка — сохраняет и пересчитывает среднее")
    void rateOrUpdate_success() {
        RatingRequest req = new RatingRequest();
        req.setRating(4);
        req.setReview("Great course!");

        when(progressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(progressWithLesson));
        when(ratingRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(ratingRepository.save(any(CourseRating.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseRating existing = new CourseRating();
        existing.setRating(4);
        when(ratingRepository.findByCourseId("course-1")).thenReturn(List.of(existing));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseRating result = courseRatingService.rateOrUpdate("user-1", "course-1", req);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getReview()).isEqualTo("Great course!");
        verify(ratingRepository).save(any(CourseRating.class));
    }

    @Test
    @DisplayName("rateOrUpdate: рейтинг < 1 — BadRequestException")
    void rateOrUpdate_ratingTooLow_throws() {
        RatingRequest req = new RatingRequest();
        req.setRating(0);

        assertThatThrownBy(() -> courseRatingService.rateOrUpdate("user-1", "course-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("between 1 and 5");
    }

    @Test
    @DisplayName("rateOrUpdate: рейтинг > 5 — BadRequestException")
    void rateOrUpdate_ratingTooHigh_throws() {
        RatingRequest req = new RatingRequest();
        req.setRating(6);

        assertThatThrownBy(() -> courseRatingService.rateOrUpdate("user-1", "course-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("rateOrUpdate: нет прогресса (не записан) — ForbiddenException")
    void rateOrUpdate_noProgress_throws() {
        RatingRequest req = new RatingRequest();
        req.setRating(3);

        when(progressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseRatingService.rateOrUpdate("user-1", "course-1", req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("enrolled");
    }

    @Test
    @DisplayName("rateOrUpdate: ни одного урока не завершено — ForbiddenException")
    void rateOrUpdate_noCompletedLessons_throws() {
        RatingRequest req = new RatingRequest();
        req.setRating(3);

        CourseProgress emptyProgress = new CourseProgress();
        emptyProgress.setCompletedLessonIds(new HashSet<>());

        when(progressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(emptyProgress));

        assertThatThrownBy(() -> courseRatingService.rateOrUpdate("user-1", "course-1", req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("at least one lesson");
    }

    @Test
    @DisplayName("rateOrUpdate: обновление существующего рейтинга")
    void rateOrUpdate_updatesExisting() {
        RatingRequest req = new RatingRequest();
        req.setRating(5);
        req.setReview("Even better now");

        CourseRating existing = new CourseRating();
        existing.setUserId("user-1");
        existing.setCourseId("course-1");
        existing.setRating(3);

        when(progressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(progressWithLesson));
        when(ratingRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existing));
        when(ratingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ratingRepository.findByCourseId("course-1")).thenReturn(List.of(existing));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseRating result = courseRatingService.rateOrUpdate("user-1", "course-1", req);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getReview()).isEqualTo("Even better now");
    }


    @Test
    @DisplayName("getRatings: возвращает все рейтинги курса")
    void getRatings_returnsAll() {
        CourseRating r1 = new CourseRating(); r1.setRating(4);
        CourseRating r2 = new CourseRating(); r2.setRating(5);

        when(ratingRepository.findByCourseId("course-1")).thenReturn(List.of(r1, r2));

        List<CourseRating> result = courseRatingService.getRatings("course-1");

        assertThat(result).hasSize(2);
    }


    @Test
    @DisplayName("deleteRating: успешное удаление — пересчитывает среднее")
    void deleteRating_success() {
        CourseRating existing = new CourseRating();
        existing.setUserId("user-1");
        existing.setCourseId("course-1");

        when(ratingRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existing));
        when(ratingRepository.findByCourseId("course-1")).thenReturn(List.of());
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        courseRatingService.deleteRating("user-1", "course-1");

        verify(ratingRepository).delete(existing);
        verify(courseRepository).save(argThat(c -> c.getRatingCount() == 0 && c.getAvgRating() == 0.0));
    }

    @Test
    @DisplayName("deleteRating: рейтинг не найден — ResourceNotFoundException")
    void deleteRating_notFound_throws() {
        when(ratingRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseRatingService.deleteRating("user-1", "course-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("recalcCourseAverage: среднее округляется до 1 знака после запятой")
    void recalc_averageRoundedCorrectly() {
        RatingRequest req = new RatingRequest();
        req.setRating(4);

        CourseRating r1 = new CourseRating(); r1.setRating(4);
        CourseRating r2 = new CourseRating(); r2.setRating(3);
        CourseRating r3 = new CourseRating(); r3.setRating(5);

        when(progressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(progressWithLesson));
        when(ratingRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(ratingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ratingRepository.findByCourseId("course-1")).thenReturn(List.of(r1, r2, r3));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        courseRatingService.rateOrUpdate("user-1", "course-1", req);

        // avg(4,3,5) = 4.0
        verify(courseRepository).save(argThat(c ->
                c.getAvgRating() == 4.0 && c.getRatingCount() == 3));
    }
}