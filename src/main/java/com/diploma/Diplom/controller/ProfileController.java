package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.ProfileResponse;
import com.diploma.Diplom.dto.UpdateProfileRequest;
import com.diploma.Diplom.service.CloudinaryService;
import com.diploma.Diplom.service.ProfileService;
import com.diploma.Diplom.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile", description = "View and update your profile")
@SecurityRequirement(name = "bearerAuth")
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

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ProfileResponse getMyProfile() {
        String userId = securityUtils.getCurrentUserId();
        return profileService.getMyProfile(userId);
    }

    @Operation(summary = "Update name and age")
    @PutMapping("/me")
    public void updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String userId = securityUtils.getCurrentUserId();
        profileService.updateProfile(userId, request);
    }

    @Operation(
        summary = "Upload avatar image",
        description = "Uploads image to Cloudinary and updates the profile picture URL. " +
                      "This is the only way to change the avatar — the URL cannot be set directly."
    )
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadAvatar(@RequestPart("file") MultipartFile file) {
        String userId = securityUtils.getCurrentUserId();
        CloudinaryService.FileUploadResult result = cloudinaryService.uploadFile(file, "avatars");
        profileService.updateAvatar(userId, result.getFileUrl());
        return profileService.getMyProfile(userId);
    }
}