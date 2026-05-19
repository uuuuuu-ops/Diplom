package com.diploma.Diplom.controller;

import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.GlobalExceptionHandler;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
@DisplayName("CourseController Integration Tests")
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // ─────────────────────── GET /courses/public ─────────────────────────
    // ИСПРАВЛЕНО: контроллер возвращает Page<Course>, а не List<Course>
    // JSON структура: $.content[0].title, $.totalElements

    @Test
    @DisplayName("GET /courses/public: No auth required — 200 OK с page контентом")
    void getPublicCourses_noAuth_ok() throws Exception {
        Course c = new Course();
        c.setId("c1");
        c.setTitle("Java Basics");

        Page<Course> page = new PageImpl<>(List.of(c), PageRequest.of(0, 20), 1);
        when(courseService.getPublicCourses(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/courses/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Java Basics"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /courses/public: с фильтром category — передаётся в сервис")
    void getPublicCourses_withCategoryFilter_ok() throws Exception {
        when(courseService.getPublicCourses(eq("IT"), isNull(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/courses/public").param("category", "IT"))
                .andExpect(status().isOk());

        verify(courseService).getPublicCourses(eq("IT"), isNull(), any());
    }

    // ─────────────────────── GET /courses/{id} ───────────────────────────

    @Test
    @DisplayName("GET /courses/{id}: Student access — 200 OK")
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void getCourseById_authenticated_ok() throws Exception {
        Course c = new Course();
        c.setId("course-1");
        c.setTitle("Python 101");

        when(courseService.getCourseById("course-1")).thenReturn(c);

        mockMvc.perform(get("/courses/course-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Python 101"));
    }

    @Test
    @DisplayName("GET /courses/{id}: Not found — 404")
    @WithMockUser(username = "student@test.com")
    void getCourseById_notFound() throws Exception {
        when(courseService.getCourseById("missing"))
                .thenThrow(new ResourceNotFoundException("Course not found"));

        mockMvc.perform(get("/courses/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course not found"));
    }

    // ─────────────────────── GET /courses/my ─────────────────────────────
    // ИСПРАВЛЕНО: контроллер передаёт email (auth.getName()) в courseService.getTeacherCourses

    @Test
    @DisplayName("GET /courses/my: Teacher access — 200 OK")
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void getMyCourses_asTeacher_ok() throws Exception {
        Course c = new Course();
        c.setTitle("My Java Course");

        // Контроллер передаёт auth.getName() = "teacher@test.com" в сервис
        when(courseService.getTeacherCourses("teacher@test.com")).thenReturn(List.of(c));

        mockMvc.perform(get("/courses/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("My Java Course"));
    }

    @Test
    @DisplayName("GET /courses/my: Student access — 403 Forbidden")
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void getMyCourses_asStudent_forbidden() throws Exception {
        mockMvc.perform(get("/courses/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /courses/my: без аутентификации — 401")
    void getMyCourses_noAuth_unauthorized() throws Exception {
        mockMvc.perform(get("/courses/my"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────── DELETE /courses/{id} ────────────────────────

    @Test
    @DisplayName("DELETE /courses/{id}: Teacher owner — 200 OK")
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void deleteCourse_asOwner_ok() throws Exception {
        doNothing().when(courseService).deleteCourse("teacher@test.com", "course-1");

        mockMvc.perform(delete("/courses/course-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Course deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /courses/{id}: Teacher not owner — 403 Forbidden")
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void deleteCourse_notOwner_forbidden() throws Exception {
        doThrow(new ForbiddenException("Not your course"))
                .when(courseService).deleteCourse("teacher@test.com", "course-99");

        mockMvc.perform(delete("/courses/course-99"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not your course"));
    }

    @Test
    @DisplayName("DELETE /courses/{id}: без аутентификации — 401")
    void deleteCourse_noAuth_unauthorized() throws Exception {
        mockMvc.perform(delete("/courses/course-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /courses/{id}: STUDENT — 403 Forbidden")
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void deleteCourse_asStudent_forbidden() throws Exception {
        mockMvc.perform(delete("/courses/course-1"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────── POST /courses ───────────────────────────────

    @Test
    @DisplayName("POST /courses: без аутентификации — 401")
    void createCourse_noAuth_unauthorized() throws Exception {
        mockMvc.perform(multipart("/courses")
                        .param("title", "Test")
                        .param("description", "Test description")
                        .param("category", "IT")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /courses: STUDENT — 403 Forbidden")
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void createCourse_asStudent_forbidden() throws Exception {
        mockMvc.perform(multipart("/courses")
                        .param("title", "Test")
                        .param("description", "Test description")
                        .param("category", "IT")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }
}
