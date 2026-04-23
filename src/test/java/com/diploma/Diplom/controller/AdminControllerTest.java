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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock UserRepository userRepository;
    @Mock CourseRepository courseRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock PaymentRepository paymentRepository;

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
        approved.setStatus(PaymentStatus.APPROVED);
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

    @Test
    void deleteUser_callsRepository() {
        adminController.deleteUser("user-123");
        verify(userRepository).deleteById("user-123");
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        User u1 = new User(); u1.setEmail("a@test.com");
        User u2 = new User(); u2.setEmail("b@test.com");
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        Object result = adminController.getAllUsers();

        assertThat((List<?>) result).hasSize(2);
    }
}