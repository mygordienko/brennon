package com.mygordienko.brennon.profile.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mygordienko.brennon.profile.adapter.rest.ProfileImageUrlResolver;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;

@Controller
public class CurrentUserProfileController {
    public CurrentUserProfileController(
            BrennonUserProfileService userProfileService,
            ProfileImageUrlResolver profileImageUrlResolver) {
        this.userProfileService = userProfileService;
        this.profileImageUrlResolver = profileImageUrlResolver;
    }

    private final BrennonUserProfileService userProfileService;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    @GetMapping("/current-user-profile")
    public String getProfileInfo(Model model) {
        var userProfile = userProfileService.getCurrentUser();

        var dto = new CurrentUserProfileDto(
            userProfile.getId().toString(),
            userProfile.getFullName(),
            userProfile.getLogin().toString(),
            profileImageUrlResolver.resolve(userProfile).orElse(null));
        model.addAttribute("currentUser", dto);
        return "profile/current-user-profile :: currentUserProfile";
    }
}
