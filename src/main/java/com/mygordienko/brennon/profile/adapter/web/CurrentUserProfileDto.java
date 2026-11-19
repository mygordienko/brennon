package com.mygordienko.brennon.profile.adapter.web;

public record CurrentUserProfileDto(
    String id,
    String fullName,
    String email,
    boolean hasProfileImage
) {
}
