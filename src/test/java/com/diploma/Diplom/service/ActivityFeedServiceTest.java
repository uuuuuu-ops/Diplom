package com.diploma.Diplom.service;

import com.diploma.Diplom.model.ActivityFeed;
import com.diploma.Diplom.model.ActivityType;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityFeedService Tests")
class ActivityFeedServiceTest {

    @Mock ActivityFeedRepository repository;

    @InjectMocks
    ActivityFeedService activityFeedService;

    @Test
    @DisplayName("addActivity: сохраняет активность с правильными полями")
    void addActivity_savesWithCorrectFields() {
        activityFeedService.addActivity(
                "user-1", ActivityType.ENROLLMENT, "course-1", "Enrolled in course");

        ArgumentCaptor<ActivityFeed> captor = ArgumentCaptor.forClass(ActivityFeed.class);
        verify(repository).save(captor.capture());

        ActivityFeed saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getType()).isEqualTo(ActivityType.ENROLLMENT.name());
        assertThat(saved.getReferenceId()).isEqualTo("course-1");
        assertThat(saved.getMessage()).isEqualTo("Enrolled in course");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("addActivity: разные типы активности — сохраняются корректно")
    void addActivity_certificate_savesCorrectType() {
        activityFeedService.addActivity(
                "user-2", ActivityType.CERTIFICATE, "course-2", "Earned certificate");

        ArgumentCaptor<ActivityFeed> captor = ArgumentCaptor.forClass(ActivityFeed.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("CERTIFICATE");
    }

    @Test
    @DisplayName("getRecentActivity: возвращает последние 20 записей")
    void getRecentActivity_returnsPageContent() {
        ActivityFeed a1 = new ActivityFeed();
        a1.setUserId("user-1");
        a1.setType("ENROLLMENT");

        ActivityFeed a2 = new ActivityFeed();
        a2.setUserId("user-1");
        a2.setType("LIKE");

        when(repository.findByUserId(eq("user-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(a1, a2)));

        List<ActivityFeed> result = activityFeedService.getRecentActivity("user-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo("ENROLLMENT");
    }

    @Test
    @DisplayName("getRecentActivity: нет активности — пустой список")
    void getRecentActivity_empty_returnsEmpty() {
        when(repository.findByUserId(eq("user-1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<ActivityFeed> result = activityFeedService.getRecentActivity("user-1");

        assertThat(result).isEmpty();
    }
}