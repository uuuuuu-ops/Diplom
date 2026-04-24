package com.diploma.Diplom.controller;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.*;
import com.diploma.Diplom.model.PaymentStatus;
import com.diploma.Diplom.model.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diploma.Diplom.util.SecurityUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock UserRepository userRepository;
    @Mock CourseRepository courseRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks AdminController adminController;

    @Test
    void getStats_returnsCorrectCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(7L);
        when(userRepository.countByRole(Role.TEACHER)).thenReturn(3L);
        when(courseRepository.count()).thenReturn(5L);
        when(enrollmentRepository.count()).thenReturn(20L);
        when(paymentRepository.findAll()).thenReturn(List.of());

        Map<String, Object> stats = adminController.getStats();

        assertThat(stats.get("totalUsers")).isEqualTo(10L);
        assertThat(stats.get("totalStudents")).isEqualTo(7L);
        assertThat(stats.get("totalTeachers")).isEqualTo(3L);
        assertThat(stats.get("totalCourses")).isEqualTo(5L);
        assertThat(stats.get("totalEnrollments")).isEqualTo(20L);
    }

    @Test
    void getStats_calculatesRevenue_onlyApprovedPayments() {
        Payment approved = new Payment();
        approved.setStatus(PaymentStatus.CAPTURED);
        approved.setAmount(new BigDecimal("99.99"));

        Payment pending = new Payment();
        pending.setStatus(PaymentStatus.CREATED);
        pending.setAmount(new BigDecimal("50.00"));

        when(paymentRepository.findAll()).thenReturn(List.of(approved, pending));
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByRole(any())).thenReturn(0L);
        when(courseRepository.count()).thenReturn(0L);
        when(enrollmentRepository.count()).thenReturn(0L);

        Map<String, Object> stats = adminController.getStats();

        assertThat((double) stats.get("totalRevenue")).isEqualTo(99.99);
    }

    // ─────────────────────── deleteUser (updated guards) ─────────────────

    @Test
    void deleteUser_success_deletesStudent() {
        User target = new User();
        target.setId("user-123");
        target.setEmail("student@test.com");
        target.setRole(Role.STUDENT);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(target));
        when(securityUtils.getCurrentUserId()).thenReturn("admin-1");

        org.springframework.security.core.Authentication auth =
                mock(org.springframework.security.core.Authentication.class);

        Map<String, String> result = adminController.deleteUser("user-123", auth);
        verify(userRepository).deleteById("user-123");
        assertThat(result.get("message")).isEqualTo("User deleted");
    }

    @Test
    void deleteUser_cannotDeleteSelf_throws() {
        User self = new User();
        self.setId("admin-1");
        self.setEmail("admin@test.com");
        self.setRole(Role.ADMIN);

        when(userRepository.findById("admin-1")).thenReturn(Optional.of(self));
        when(securityUtils.getCurrentUserId()).thenReturn("admin-1");

        org.springframework.security.core.Authentication auth =
                mock(org.springframework.security.core.Authentication.class);

        assertThatThrownBy(() -> adminController.deleteUser("admin-1", auth))
                .isInstanceOf(com.diploma.Diplom.exception.BadRequestException.class)
                .hasMessageContaining("own account");
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_cannotDeleteAnotherAdmin_throws() {
        User otherAdmin = new User();
        otherAdmin.setId("admin-2");
        otherAdmin.setEmail("other@test.com");
        otherAdmin.setRole(Role.ADMIN);

        when(userRepository.findById("admin-2")).thenReturn(Optional.of(otherAdmin));
        when(securityUtils.getCurrentUserId()).thenReturn("admin-1");

        org.springframework.security.core.Authentication auth =
                mock(org.springframework.security.core.Authentication.class);

        assertThatThrownBy(() -> adminController.deleteUser("admin-2", auth))
                .isInstanceOf(com.diploma.Diplom.exception.BadRequestException.class)
                .hasMessageContaining("administrator");
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_userNotFound_throws() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        org.springframework.security.core.Authentication auth =
                mock(org.springframework.security.core.Authentication.class);

        assertThatThrownBy(() -> adminController.deleteUser("missing", auth))
                .isInstanceOf(com.diploma.Diplom.exception.ResourceNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }

    // ─────────────────────── getAllUsers (no passwords) ───────────────────

    @Test
    void getAllUsers_returnsUserViews_withoutPasswords() {
        User u1 = new User(); u1.setEmail("a@test.com"); u1.setRole(Role.STUDENT); u1.setPassword("hashed");
        User u2 = new User(); u2.setEmail("b@test.com"); u2.setRole(Role.TEACHER); u2.setPassword("hashed2");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<AdminController.UserView> result = adminController.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AdminController.UserView::email)
                .containsExactlyInAnyOrder("a@test.com", "b@test.com");
    }
}