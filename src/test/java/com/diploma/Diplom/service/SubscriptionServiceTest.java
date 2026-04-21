package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.messaging.SubscriptionProducer;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.SubscriptionRepository;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService Tests")
class SubscriptionServiceTest {

    @Mock SubscriptionRepository subscriptionRepository;
    @Mock UserRepository userRepository;
    @Mock SubscriptionProducer subscriptionProducer;
    @Mock SecurityUtils securityUtils;

    @InjectMocks
    SubscriptionService subscriptionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setEmail("user@test.com");
    }

    // ─────────────────────── hasActiveSubscription ───────────────────────

    @Test
    @DisplayName("hasActiveSubscription: активная подписка есть — true")
    void hasActiveSubscription_active_returnsTrue() {
        when(subscriptionRepository.existsByUserIdAndStatus("user-1", SubscriptionStatus.ACTIVE))
                .thenReturn(true);

        assertThat(subscriptionService.hasActiveSubscription("user-1")).isTrue();
    }

    @Test
    @DisplayName("hasActiveSubscription: нет активной подписки — false")
    void hasActiveSubscription_inactive_returnsFalse() {
        when(subscriptionRepository.existsByUserIdAndStatus("user-1", SubscriptionStatus.ACTIVE))
                .thenReturn(false);

        assertThat(subscriptionService.hasActiveSubscription("user-1")).isFalse();
    }

    // ─────────────────────── createPendingSubscription ───────────────────

    @Test
    @DisplayName("createPendingSubscription: сохраняет подписку в статусе APPROVAL_PENDING")
    void createPendingSubscription_success() {
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Subscription result = subscriptionService.createPendingSubscription(
                "user-1", "BASIC", "plan-abc", "sub-xyz");

        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getPlanCode()).isEqualTo("BASIC");
        assertThat(result.getPaypalPlanId()).isEqualTo("plan-abc");
        assertThat(result.getPaypalSubscriptionId()).isEqualTo("sub-xyz");
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.APPROVAL_PENDING);
        assertThat(result.getProvider()).isEqualTo("PAYPAL");
    }

    // ─────────────────────── activateSubscription ────────────────────────

    @Test
    @DisplayName("activateSubscription: устанавливает статус ACTIVE и шлёт событие")
    void activateSubscription_success() {
        Subscription sub = new Subscription();
        sub.setUserId("user-1");
        sub.setPaypalSubscriptionId("I-ABCDEF");
        sub.setStatus(SubscriptionStatus.APPROVAL_PENDING);
        sub.setPlanCode("BASIC");

        when(subscriptionRepository.findByPaypalSubscriptionId("I-ABCDEF"))
                .thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        Subscription result = subscriptionService.activateSubscription("I-ABCDEF");

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.getStartedAt()).isNotNull();
        verify(subscriptionProducer).sendSubscriptionEvent(
                "user-1", "user@test.com", "ACTIVATED", "BASIC");
    }

    @Test
    @DisplayName("activateSubscription: подписка не найдена — ResourceNotFoundException")
    void activateSubscription_notFound_throws() {
        when(subscriptionRepository.findByPaypalSubscriptionId("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.activateSubscription("unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("activateSubscription: пользователь не найден — событие не отправляется")
    void activateSubscription_userNotFound_noEvent() {
        Subscription sub = new Subscription();
        sub.setUserId("ghost-user");
        sub.setPaypalSubscriptionId("I-GHOST");
        sub.setStatus(SubscriptionStatus.APPROVAL_PENDING);
        sub.setPlanCode("PRO");

        when(subscriptionRepository.findByPaypalSubscriptionId("I-GHOST"))
                .thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById("ghost-user")).thenReturn(Optional.empty());

        subscriptionService.activateSubscription("I-GHOST");

        verifyNoInteractions(subscriptionProducer);
    }

    // ─────────────────────── cancelSubscription ──────────────────────────

    @Test
    @DisplayName("cancelSubscription: устанавливает статус CANCELLED и шлёт событие")
    void cancelSubscription_success() {
        Subscription sub = new Subscription();
        sub.setUserId("user-1");
        sub.setPaypalSubscriptionId("I-ABCDEF");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setPlanCode("BASIC");

        when(subscriptionRepository.findByPaypalSubscriptionId("I-ABCDEF"))
                .thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        Subscription result = subscriptionService.cancelSubscription("I-ABCDEF");

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(result.getEndedAt()).isNotNull();
        verify(subscriptionProducer).sendSubscriptionEvent(
                "user-1", "user@test.com", "CANCELLED", "BASIC");
    }

    // ─────────────────────── getMySubscriptions ──────────────────────────

    @Test
    @DisplayName("getMySubscriptions: возвращает подписки текущего пользователя")
    void getMySubscriptions_returnsList() {
        Subscription s1 = new Subscription(); s1.setUserId("user-1");
        Subscription s2 = new Subscription(); s2.setUserId("user-1");

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(subscriptionRepository.findByUserId("user-1")).thenReturn(List.of(s1, s2));

        List<Subscription> result = subscriptionService.getMySubscriptions();

        assertThat(result).hasSize(2);
    }
}