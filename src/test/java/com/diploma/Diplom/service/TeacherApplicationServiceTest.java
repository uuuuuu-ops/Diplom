package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.TeacherApplication;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.TeacherApplicationRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherApplicationServiceTest {

    @Mock TeacherApplicationRepository applicationRepository;
    @Mock UserRepository userRepository;

    @InjectMocks TeacherApplicationService teacherApplicationService;

    @Test
    void getPendingApplications_returnsPendingOnly() {
        TeacherApplication app = new TeacherApplication();
        app.setStatus("PENDING");

        when(applicationRepository.findByStatus("PENDING")).thenReturn(List.of(app));

        List<TeacherApplication> result = teacherApplicationService.getPendingApplications();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void approveApplication_setsStatusAndApprovesTeacher() {
        User teacher = new User();
        teacher.setId("teacher-1");
        teacher.setTeacherApproved(false);

        TeacherApplication app = new TeacherApplication();
        app.setId("app-1");
        app.setUserId("teacher-1");
        app.setStatus("PENDING");

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        teacherApplicationService.approveApplication("app-1", "Good candidate");

        assertThat(app.getStatus()).isEqualTo("APPROVED");
        assertThat(teacher.isTeacherApproved()).isTrue();
    }

    @Test
    void approveApplication_notFound_throws() {
        when(applicationRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherApplicationService.approveApplication(
    "missing", "comment"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectApplication_setsStatusRejected() {
        User teacher = new User();
        teacher.setId("teacher-1");

        TeacherApplication app = new TeacherApplication();
        app.setId("app-1");
        app.setUserId("teacher-1");
        app.setStatus("PENDING");

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher)); 
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        teacherApplicationService.rejectApplication("app-1", "Not qualified");

        assertThat(app.getStatus()).isEqualTo("REJECTED");
    }
}