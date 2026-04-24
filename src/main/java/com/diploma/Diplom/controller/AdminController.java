package com.diploma.Diplom.controller;

import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.*;
import com.diploma.Diplom.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard — доступно только для роли ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;


    public record UserView(
        String id,
        String name,
        String email,
        Role role,
        boolean enabled,
        boolean teacherApproved,
        LocalDateTime createdAt
    ) {}

    @Operation(
        summary = "Общая статистика платформы",
        description = "Возвращает количество пользователей, курсов, записей и общую выручку"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Статистика получена"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён — требуется роль ADMIN")
    })
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        long totalUsers       = userRepository.count();
        long totalStudents    = userRepository.countByRole(Role.STUDENT);
        long totalTeachers    = userRepository.countByRole(Role.TEACHER);
        long totalCourses     = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        double totalRevenue = paymentRepository.findAll().stream()
            .filter(p -> p.getStatus() == PaymentStatus.CAPTURED)
            .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0)
            .sum();

        return Map.of(
            "totalUsers", totalUsers,
            "totalStudents", totalStudents,
            "totalTeachers", totalTeachers,
            "totalCourses", totalCourses,
            "totalEnrollments", totalEnrollments,
            "totalRevenue", totalRevenue
        );
    }

    @Operation(
        summary = "Список всех пользователей",
        description = "Возвращает всех зарегистрированных пользователей платформы"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список пользователей получен"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён — требуется роль ADMIN")
    })
    @GetMapping("/users")
    public List<UserView> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> new UserView(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.isEnabled(),
                u.isTeacherApproved(),
                u.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    @Operation(
        summary = "Удалить пользователя",
        description = """
            Удаляет пользователя по ID. Действие необратимо.

            Ограничения:
            - Нельзя удалить собственный аккаунт
            - Нельзя удалить другого администратора
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Пользователь удалён"),
        @ApiResponse(responseCode = "400", description = "Нельзя удалить себя или другого администратора"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещён — требуется роль ADMIN"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(@PathVariable String id, Authentication authentication) {
        User target = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String currentUserId = securityUtils.getCurrentUserId();

        
        if (currentUserId.equals(target.getId())) {
            throw new BadRequestException("Cannot delete your own account");
        }

        if (target.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot delete an administrator account");
        }

        userRepository.deleteById(id);
        return Map.of("message", "User deleted");
    }
}