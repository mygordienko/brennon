package com.mygordienko.brennon.core.auth.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;

@Controller
public class CurrentUserProfileController {
    public CurrentUserProfileController(BrennonUserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private final BrennonUserProfileService userProfileService;

    @GetMapping("/current-user-profile")
    public String getProfileInfo(Model model) {
        var userProfile = userProfileService.getCurrentUser();

        var dto = new UserInfoDto(userProfile.getId().toString(), userProfile.getFullName(), userProfile.getLogin().toString());
        model.addAttribute("currentUser", dto);
        return "core/auth/current-user-profile :: currentUserProfile";
    }
}
