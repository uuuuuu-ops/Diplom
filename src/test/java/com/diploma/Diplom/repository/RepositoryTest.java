package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@DataMongoTest
@DisplayName("Repository Tests — MongoDB Queries")
class RepositoryTest {

    @Autowired CourseRepository courseRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired UserRepository userRepository;
    @Autowired QuizRepository quizRepository;
    @Autowired LessonRepository lessonRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired CourseRatingRepository courseRatingRepository;
    @Autowired CourseProgressRepository courseProgressRepository;
    @Autowired QuizAttemptRepository quizAttemptRepository;

    @AfterEach
    void cleanUp() {
        courseRepository.deleteAll();
        enrollmentRepository.deleteAll();
        userRepository.deleteAll();
        quizRepository.deleteAll();
        lessonRepository.deleteAll();
        subscriptionRepository.deleteAll();
        courseRatingRepository.deleteAll();
        courseProgressRepository.deleteAll();
        quizAttemptRepository.deleteAll();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CourseRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("CourseRepository.findByPublishedTrue: возвращает только опубликованные")
    void courseRepo_findByPublishedTrue() {
        Course published = new Course();
        published.setTitle("Published");
        published.setPublished(true);
        published.setTeacherId("t1");

        Course draft = new Course();
        draft.setTitle("Draft");
        draft.setPublished(false);
        draft.setTeacherId("t1");

        courseRepository.saveAll(List.of(published, draft));

        List<Course> result = courseRepository.findByPublishedTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Published");
    }

    @Test
    @DisplayName("CourseRepository.findByTeacherId: возвращает курсы конкретного преподавателя")
    void courseRepo_findByTeacherId() {
        Course c1 = new Course(); c1.setTitle("C1"); c1.setTeacherId("teacher-A");
        Course c2 = new Course(); c2.setTitle("C2"); c2.setTeacherId("teacher-A");
        Course c3 = new Course(); c3.setTitle("C3"); c3.setTeacherId("teacher-B");

        courseRepository.saveAll(List.of(c1, c2, c3));

        List<Course> result = courseRepository.findByTeacherId("teacher-A");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(c -> "teacher-A".equals(c.getTeacherId()));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EnrollmentRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("EnrollmentRepository.findByUserIdAndCourseIdAndStatus: находит активную запись")
    void enrollmentRepo_findByUserCourseStatus_found() {
        Enrollment e = new Enrollment();
        e.setUserId("user-1");
        e.setCourseId("course-1");
        e.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(e);

        Optional<Enrollment> result = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus("user-1", "course-1", EnrollmentStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("EnrollmentRepository.findByUserIdAndCourseIdAndStatus: не находит с другим статусом")
    void enrollmentRepo_findByUserCourseStatus_differentStatus() {
        Enrollment e = new Enrollment();
        e.setUserId("user-1");
        e.setCourseId("course-1");
        e.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(e);

        Optional<Enrollment> result = enrollmentRepository
                .findByUserIdAndCourseIdAndStatus("user-1", "course-1", EnrollmentStatus.REVOKED);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("EnrollmentRepository.existsByUserIdAndCourseIdAndStatus: возвращает true если есть")
    void enrollmentRepo_existsByUserCourseStatus_true() {
        Enrollment e = new Enrollment();
        e.setUserId("user-1");
        e.setCourseId("course-1");
        e.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(e);

        boolean exists = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus("user-1", "course-1", EnrollmentStatus.ACTIVE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("EnrollmentRepository.existsByUserIdAndCourseIdAndStatus: возвращает false если нет")
    void enrollmentRepo_existsByUserCourseStatus_false() {
        boolean exists = enrollmentRepository
                .existsByUserIdAndCourseIdAndStatus("user-1", "course-1", EnrollmentStatus.ACTIVE);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("EnrollmentRepository.findByUserId: возвращает все записи пользователя")
    void enrollmentRepo_findByUserId() {
        Enrollment e1 = new Enrollment(); e1.setUserId("user-1"); e1.setCourseId("c1"); e1.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment e2 = new Enrollment(); e2.setUserId("user-1"); e2.setCourseId("c2"); e2.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment e3 = new Enrollment(); e3.setUserId("user-2"); e3.setCourseId("c1"); e3.setStatus(EnrollmentStatus.ACTIVE);

        enrollmentRepository.saveAll(List.of(e1, e2, e3));

        List<Enrollment> result = enrollmentRepository.findByUserId("user-1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> "user-1".equals(e.getUserId()));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UserRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("UserRepository.findByEmail: находит пользователя по email")
    void userRepo_findByEmail_found() {
        User user = new User();
        user.setEmail("alice@test.com");
        user.setName("Alice");
        user.setRole(Role.STUDENT);
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmail("alice@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("UserRepository.findByEmail: не находит несуществующий email")
    void userRepo_findByEmail_notFound() {
        Optional<User> result = userRepository.findByEmail("ghost@test.com");

        assertThat(result).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LessonRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("LessonRepository.findByCourseIdOrderByOrderIndexAsc: правильный порядок")
    void lessonRepo_findByCourseIdOrdered() {
        Lesson l1 = new Lesson(); l1.setCourseId("c1"); l1.setTitle("Lesson 3"); l1.setOrderIndex(2);
        Lesson l2 = new Lesson(); l2.setCourseId("c1"); l2.setTitle("Lesson 1"); l2.setOrderIndex(0);
        Lesson l3 = new Lesson(); l3.setCourseId("c1"); l3.setTitle("Lesson 2"); l3.setOrderIndex(1);

        lessonRepository.saveAll(List.of(l1, l2, l3));

        List<Lesson> result = lessonRepository.findByCourseIdOrderByOrderIndexAsc("c1");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getOrderIndex()).isEqualTo(0);
        assertThat(result.get(1).getOrderIndex()).isEqualTo(1);
        assertThat(result.get(2).getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("LessonRepository.findByCourseIdOrderByOrderIndexAsc: уроки другого курса не включаются")
    void lessonRepo_findByCourseId_excludesOtherCourses() {
        Lesson l1 = new Lesson(); l1.setCourseId("c1"); l1.setOrderIndex(0);
        Lesson l2 = new Lesson(); l2.setCourseId("c2"); l2.setOrderIndex(0);

        lessonRepository.saveAll(List.of(l1, l2));

        List<Lesson> result = lessonRepository.findByCourseIdOrderByOrderIndexAsc("c1");

        assertThat(result).hasSize(1);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QuizRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("QuizRepository.findByLessonId: находит квиз по уроку")
    void quizRepo_findByLessonId_found() {
        Quiz quiz = new Quiz();
        quiz.setLessonId("lesson-1");
        quiz.setTitle("Quiz 1");
        quizRepository.save(quiz);

        Optional<Quiz> result = quizRepository.findByLessonId("lesson-1");

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Quiz 1");
    }

    @Test
    @DisplayName("QuizRepository.findByLessonId: пусто если нет квиза")
    void quizRepo_findByLessonId_empty() {
        Optional<Quiz> result = quizRepository.findByLessonId("no-such-lesson");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("QuizRepository.findByLessonIdIn: возвращает квизы для нескольких уроков")
    void quizRepo_findByLessonIdIn() {
        Quiz q1 = new Quiz(); q1.setLessonId("l1"); q1.setTitle("Q1");
        Quiz q2 = new Quiz(); q2.setLessonId("l2"); q2.setTitle("Q2");
        Quiz q3 = new Quiz(); q3.setLessonId("l3"); q3.setTitle("Q3");
        quizRepository.saveAll(List.of(q1, q2, q3));

        List<Quiz> result = quizRepository.findByLessonIdIn(List.of("l1", "l2"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Quiz::getLessonId).containsExactlyInAnyOrder("l1", "l2");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SubscriptionRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("SubscriptionRepository.existsByUserIdAndStatus: активная подписка существует")
    void subscriptionRepo_existsByUserIdAndStatus_true() {
        Subscription sub = new Subscription();
        sub.setUserId("user-1");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);

        boolean exists = subscriptionRepository
                .existsByUserIdAndStatus("user-1", SubscriptionStatus.ACTIVE);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("SubscriptionRepository.existsByUserIdAndStatus: отменённая подписка не считается активной")
    void subscriptionRepo_existsByUserIdAndStatus_cancelledNotActive() {
        Subscription sub = new Subscription();
        sub.setUserId("user-1");
        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);

        boolean exists = subscriptionRepository
                .existsByUserIdAndStatus("user-1", SubscriptionStatus.ACTIVE);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("SubscriptionRepository.findByUserIdAndStatus: находит подписку по userId и статусу")
    void subscriptionRepo_findByUserIdAndStatus() {
        Subscription sub = new Subscription();
        sub.setUserId("user-1");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setPaypalSubscriptionId("paypal-sub-123");
        subscriptionRepository.save(sub);

        Optional<Subscription> result = subscriptionRepository
                .findByUserIdAndStatus("user-1", SubscriptionStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getPaypalSubscriptionId()).isEqualTo("paypal-sub-123");
    }

    @Test
    @DisplayName("SubscriptionRepository.findByPaypalSubscriptionId: находит по PayPal ID")
    void subscriptionRepo_findByPaypalSubscriptionId() {
        Subscription sub = new Subscription();
        sub.setUserId("user-1");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setPaypalSubscriptionId("I-BW452GLLEP1G");
        subscriptionRepository.save(sub);

        Optional<Subscription> result = subscriptionRepository
                .findByPaypalSubscriptionId("I-BW452GLLEP1G");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user-1");
    }

    @Test
    @DisplayName("SubscriptionRepository.findByUserId: возвращает все подписки пользователя")
    void subscriptionRepo_findByUserId_multipleSubscriptions() {
        Subscription s1 = new Subscription(); s1.setUserId("user-1"); s1.setStatus(SubscriptionStatus.CANCELLED);
        Subscription s2 = new Subscription(); s2.setUserId("user-1"); s2.setStatus(SubscriptionStatus.ACTIVE);
        Subscription s3 = new Subscription(); s3.setUserId("user-2"); s3.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.saveAll(List.of(s1, s2, s3));

        List<Subscription> result = subscriptionRepository.findByUserId("user-1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> "user-1".equals(s.getUserId()));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CourseRatingRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("CourseRatingRepository.findByUserIdAndCourseId: находит рейтинг пользователя")
    void ratingRepo_findByUserAndCourse() {
        CourseRating rating = new CourseRating();
        rating.setUserId("user-1");
        rating.setCourseId("course-1");
        rating.setRating(4);
        courseRatingRepository.save(rating);

        Optional<CourseRating> result = courseRatingRepository
                .findByUserIdAndCourseId("user-1", "course-1");

        assertThat(result).isPresent();
        assertThat(result.get().getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("CourseRatingRepository.findByCourseId: возвращает все рейтинги курса")
    void ratingRepo_findByCourseId() {
        CourseRating r1 = new CourseRating(); r1.setUserId("u1"); r1.setCourseId("c1"); r1.setRating(5);
        CourseRating r2 = new CourseRating(); r2.setUserId("u2"); r2.setCourseId("c1"); r2.setRating(3);
        CourseRating r3 = new CourseRating(); r3.setUserId("u3"); r3.setCourseId("c2"); r3.setRating(4);

        courseRatingRepository.saveAll(List.of(r1, r2, r3));

        List<CourseRating> result = courseRatingRepository.findByCourseId("c1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> "c1".equals(r.getCourseId()));
    }

    @Test
    @DisplayName("CourseRatingRepository.countByCourseId: правильный подсчёт")
    void ratingRepo_countByCourseId() {
        CourseRating r1 = new CourseRating(); r1.setUserId("u1"); r1.setCourseId("c1"); r1.setRating(4);
        CourseRating r2 = new CourseRating(); r2.setUserId("u2"); r2.setCourseId("c1"); r2.setRating(5);

        courseRatingRepository.saveAll(List.of(r1, r2));

        long count = courseRatingRepository.countByCourseId("c1");

        assertThat(count).isEqualTo(2);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CourseProgressRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("CourseProgressRepository.findByUserIdAndCourseId: находит прогресс")
    void progressRepo_findByUserAndCourse() {
        CourseProgress progress = new CourseProgress();
        progress.setUserId("user-1");
        progress.setCourseId("course-1");
        progress.setProgressPercent(50);
        progress.setCompletedLessonIds(new java.util.HashSet<>());
        progress.setPassedQuizIds(new java.util.HashSet<>());
        courseProgressRepository.save(progress);

        Optional<CourseProgress> result = courseProgressRepository
                .findByUserIdAndCourseId("user-1", "course-1");

        assertThat(result).isPresent();
        assertThat(result.get().getProgressPercent()).isEqualTo(50);
    }

    @Test
    @DisplayName("CourseProgressRepository.findByUserIdAndCourseId: пусто для другого пользователя")
    void progressRepo_findByUserAndCourse_otherUser_empty() {
        CourseProgress progress = new CourseProgress();
        progress.setUserId("user-1");
        progress.setCourseId("course-1");
        progress.setCompletedLessonIds(new java.util.HashSet<>());
        progress.setPassedQuizIds(new java.util.HashSet<>());
        courseProgressRepository.save(progress);

        Optional<CourseProgress> result = courseProgressRepository
                .findByUserIdAndCourseId("user-2", "course-1");

        assertThat(result).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  QuizAttemptRepository
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("QuizAttemptRepository.findByUserIdAndQuizId: возвращает попытки пользователя")
    void attemptRepo_findByUserAndQuiz() {
        QuizAttempt a1 = new QuizAttempt(); a1.setUserId("user-1"); a1.setQuizId("q1"); a1.setScore(80); a1.setPassed(true);
        QuizAttempt a2 = new QuizAttempt(); a2.setUserId("user-1"); a2.setQuizId("q1"); a2.setScore(40); a2.setPassed(false);
        QuizAttempt a3 = new QuizAttempt(); a3.setUserId("user-2"); a3.setQuizId("q1"); a3.setScore(90); a3.setPassed(true);

        quizAttemptRepository.saveAll(List.of(a1, a2, a3));

        List<QuizAttempt> result = quizAttemptRepository.findByUserIdAndQuizId("user-1", "q1");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> "user-1".equals(a.getUserId()));
    }

    @Test
    @DisplayName("QuizAttemptRepository.findByUserIdAndQuizId: пусто если нет попыток")
    void attemptRepo_findByUserAndQuiz_noAttempts() {
        List<QuizAttempt> result = quizAttemptRepository.findByUserIdAndQuizId("user-1", "q-missing");
        assertThat(result).isEmpty();
    }
}
