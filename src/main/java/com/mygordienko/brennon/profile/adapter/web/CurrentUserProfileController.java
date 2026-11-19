package com.mygordienko.brennon.profile.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

@Controller
public class CurrentUserProfileController {
    public CurrentUserProfileController(
            BrennonUserProfileService userProfileService,
            ProfileImageService profileImageService) {
        this.userProfileService = userProfileService;
        this.profileImageService = profileImageService;
    }

    private final BrennonUserProfileService userProfileService;
    private final ProfileImageService profileImageService;

    @GetMapping("/current-user-profile")
    public String getProfileInfo(Model model) {
        var userProfile = userProfileService.getCurrentUser();

        var dto = new CurrentUserProfileDto(
            userProfile.getId().toString(),
            userProfile.getFullName(),
            userProfile.getLogin().toString(),
            profileImageService.currentUserHasProfileImage());
        model.addAttribute("currentUser", dto);
        return "profile/current-user-profile :: currentUserProfile";
    }
}
