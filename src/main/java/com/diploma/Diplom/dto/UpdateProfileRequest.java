package com.diploma.Diplom.dto;

public record UpdateProfileRequest(
        String name,
        Integer age,
        String profileImageUrl
) {}
