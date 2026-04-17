package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ProfileResponse;
import com.diploma.Diplom.dto.UpdateProfileRequest;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService Tests")
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @Mock ActivityFeedRepository activityFeedRepository;

    @InjectMocks ProfileService profileService;

    // ─────────────────────── getMyProfile ────────────────────────────────

    @Test
    @DisplayName("getMyProfile: возвращает профиль с именем и возрастом")
    void getMyProfile_success() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alice");
        user.setAge(25);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(activityFeedRepository.findByUserIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of());

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

    // ─────────────────────── updateProfile ───────────────────────────────

    @Test
    @DisplayName("updateProfile: обновляет имя и возраст")
    void updateProfile_updatesNameAndAge() {
        User user = new User();
        user.setId("user-1");
        user.setName("Old Name");
        user.setAge(20);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        profileService.updateProfile("user-1", new UpdateProfileRequest("New Name", 30, null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Name");
        assertThat(captor.getValue().getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("updateProfile: пустое имя — имя не меняется")
    void updateProfile_blankName_keepsPreviousName() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alice");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        profileService.updateProfile("user-1", new UpdateProfileRequest("  ", null, null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("updateProfile: обновляет только profileImageUrl")
    void updateProfile_updatesImageUrl() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alice");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        profileService.updateProfile("user-1",
                new UpdateProfileRequest(null, null, "https://cdn.example.com/avatar.jpg"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getProfileImageUrl())
                .isEqualTo("https://cdn.example.com/avatar.jpg");
        assertThat(captor.getValue().getName()).isEqualTo("Alice"); // не изменилось
    }

    @Test
    @DisplayName("updateProfile: пользователь не найден — RuntimeException")
    void updateProfile_userNotFound_throws() {
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                profileService.updateProfile("ghost", new UpdateProfileRequest("Name", null, null)))
                .isInstanceOf(RuntimeException.class);
    }
}
