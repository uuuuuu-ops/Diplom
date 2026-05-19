package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ProfileResponse;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @Mock ActivityFeedRepository activityFeedRepository;

    @InjectMocks ProfileService profileService;

    @Test
    @DisplayName("getMyProfile: возвращает профиль с именем и возрастом")
    void getMyProfile_success() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alice");
        user.setAge(25);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(activityFeedRepository.findByUserIdOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq("user-1"),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        ProfileResponse result = profileService.getMyProfile("user-1");

        assertThat(result.id()).isEqualTo("user-1");
        assertThat(result.name()).isEqualTo("Alice");
        assertThat(result.age()).isEqualTo(25);
        assertThat(result.activity()).isEmpty();
    }

    @Test
    @DisplayName("getMyProfile: пользователь не найден — RuntimeException")
    void getMyProfile_userNotFound_throws() {
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getMyProfile("ghost"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}