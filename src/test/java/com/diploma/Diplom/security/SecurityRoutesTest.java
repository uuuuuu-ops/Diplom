package com.diploma.Diplom.security;

import com.diploma.Diplom.auth.AuthService;
import com.diploma.Diplom.controller.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import com.diploma.Diplom.controller.TestSecurityConfig;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        CourseController.class,
        EnrollmentController.class,
        LessonController.class,
        QuizController.class,
        QuizAttemptController.class,
        ProgressController.class,
        CourseRatingController.class,
        TeacherApplicationController.class
},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = true)
@Import(TestSecurityConfig.class)
class SecurityRoutesTest {

    @Autowired MockMvc mockMvc;

    // ── Core auth/security beans ──────────────────────────────────────────
    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    // ── Domain service beans required by controllers ──────────────────────
    @MockitoBean CourseService courseService;
    @MockitoBean EnrollmentService enrollmentService;
    @MockitoBean LessonService lessonService;
    @MockitoBean QuizService quizService;
    @MockitoBean QuizAttemptService quizAttemptService;
    @MockitoBean CourseProgressService courseProgressService;
    @MockitoBean CourseRatingService courseRatingService;
    @MockitoBean TeacherApplicationService teacherApplicationService;
    @MockitoBean RedisTemplate<String, String> redisTemplate;

    // ── TestSecurityConfig provides JwtBlacklistService + RateLimiterService mocks ──

    @Test
    @DisplayName("GET /courses/public — без аутентификации — 200 OK")
    void publicCourses_noAuth_allowed() throws Exception {
        mockMvc.perform(get("/courses/public"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/register — без аутентификации — не блокируется")
    void authRegister_noAuth_notBlocked() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name":"Test User",
                                  "email":"test@test.com",
                                  "password":"password123"
                                }
                                """))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
    }

    @Test
    @DisplayName("GET /courses/my — без аутентификации — 401")
    void myCourses_noAuth_401() throws Exception {
        mockMvc.perform(get("/courses/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /enrollments/my — без аутентификации — 401")
    void myEnrollments_noAuth_401() throws Exception {
        mockMvc.perform(get("/enrollments/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /progress/complete — без аутентификации — 401")
    void completeLesson_noAuth_401() throws Exception {
        mockMvc.perform(post("/progress/complete"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /quiz-attempts/submit — без аутентификации — 401")
    void submitQuiz_noAuth_401() throws Exception {
        mockMvc.perform(post("/quiz-attempts/submit")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /courses/course-1 — без аутентификации — 401")
    void deleteCourse_noAuth_401() throws Exception {
        mockMvc.perform(delete("/courses/course-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /courses/{id} — STUDENT — 403 Forbidden")
    void deleteCourse_asStudent_403() throws Exception {
        mockMvc.perform(delete("/courses/course-1")
                        .with(user("student@test.com").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /courses/my — STUDENT — 403 Forbidden")
    void getMyCourses_asStudent_403() throws Exception {
        mockMvc.perform(get("/courses/my")
                        .with(user("student@test.com").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /lessons/{id} — STUDENT — 403 Forbidden")
    void deleteLesson_asStudent_403() throws Exception {
        mockMvc.perform(delete("/lessons/lesson-1")
                        .with(user("student@test.com").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /quizzes/{id} — STUDENT — 403 Forbidden")
    void deleteQuiz_asStudent_403() throws Exception {
        mockMvc.perform(delete("/quizzes/quiz-1")
                        .with(user("student@test.com").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /enrollments/free/{id} — TEACHER — 403 Forbidden")
    void enrollFree_asTeacher_403() throws Exception {
        mockMvc.perform(post("/enrollments/free/course-1")
                        .with(user("teacher@test.com").roles("TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /quiz-attempts/submit — TEACHER — 403 Forbidden")
    void submitQuiz_asTeacher_403() throws Exception {
        mockMvc.perform(post("/quiz-attempts/submit")
                        .with(user("teacher@test.com").roles("TEACHER"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /courses/{id} — STUDENT — не возвращает 401/403")
    void getCourseById_asStudent_accessible() throws Exception {
        mockMvc.perform(get("/courses/course-1")
                        .with(user("student@test.com").roles("STUDENT")))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
    }

    @Test
    @DisplayName("GET /lessons/course/{id} — TEACHER — не возвращает 401/403")
    void getLessonsByCourse_asTeacher_accessible() throws Exception {
        mockMvc.perform(get("/lessons/course/course-1")
                        .with(user("teacher@test.com").roles("TEACHER")))
                .andExpect(result ->
                        assertThat(result.getResponse().getStatus()).isNotIn(401, 403));
    }
}