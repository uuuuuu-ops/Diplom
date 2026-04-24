package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.ActivityItem;
import com.diploma.Diplom.dto.ProfileResponse;
import com.diploma.Diplom.dto.UpdateProfileRequest;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ActivityFeedRepository activityFeedRepository;

    public ProfileService(UserRepository userRepository,
                          ActivityFeedRepository activityFeedRepository) {
        this.userRepository = userRepository;
        this.activityFeedRepository = activityFeedRepository;
    }

    public ProfileResponse getMyProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        
        var activity = activityFeedRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 20))
                .stream()
                .map(a -> new ActivityItem(
                        a.getType(),
                        a.getMessage(),
                        a.getCreatedAt()
                ))
                .toList();

        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getProfileImageUrl(),
                activity
        );
    }

    public void updateProfile(String userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.age() != null) {
            user.setAge(request.age());
        }
        userRepository.save(user);
    }

    public void updateAvatar(String userId, String cloudinaryUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setProfileImageUrl(cloudinaryUrl);
        userRepository.save(user);
    }
}