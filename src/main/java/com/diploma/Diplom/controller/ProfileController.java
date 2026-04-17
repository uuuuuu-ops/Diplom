package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.ProfileResponse;
import com.diploma.Diplom.dto.UpdateProfileRequest;
import com.diploma.Diplom.service.CloudinaryService;
import com.diploma.Diplom.service.ProfileService;
import com.diploma.Diplom.util.SecurityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final SecurityUtils securityUtils;
    private final CloudinaryService cloudinaryService;

    public ProfileController(ProfileService profileService,
                             SecurityUtils securityUtils,
                             CloudinaryService cloudinaryService) {
        this.profileService = profileService;
        this.securityUtils = securityUtils;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/me")
    public ProfileResponse getMyProfile() {
        String userId = securityUtils.getCurrentUserId();
        return profileService.getMyProfile(userId);
    }

    @PutMapping("/me")
    public void updateProfile(@RequestBody UpdateProfileRequest request) {
        String userId = securityUtils.getCurrentUserId();
        profileService.updateProfile(userId, request);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadAvatar(@RequestPart("file") MultipartFile file) {
        String userId = securityUtils.getCurrentUserId();
        CloudinaryService.FileUploadResult result = cloudinaryService.uploadFile(file, "avatars");
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, result.getFileUrl());
        profileService.updateProfile(userId, request);
        return profileService.getMyProfile(userId);
    }
}