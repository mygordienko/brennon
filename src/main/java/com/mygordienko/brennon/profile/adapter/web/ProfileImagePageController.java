package com.mygordienko.brennon.profile.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.profile.ProfileImageStorageException;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

@Controller
public class ProfileImagePageController {
    public ProfileImagePageController(
            ProfileImageService profileImageService,
            BrennonUserProfileService userProfileService) {
        this.profileImageService = profileImageService;
        this.userProfileService = userProfileService;
    }

    private final ProfileImageService profileImageService;
    private final BrennonUserProfileService userProfileService;

    @GetMapping("/profile")
    public String getProfile(Model model) {
        populateModel(model);
        model.addAttribute("profileImageForm", new ChangeProfileImageCommandDto());
        return "profile/profile";
    }

    @PostMapping("/profile/image")
    public String saveProfileImage(
            @Validated @ModelAttribute("profileImageForm") ChangeProfileImageCommandDto profileImageForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            populateModel(model);
            return "profile/profile";
        }

        try {
            profileImageService.saveProfileImage(FileId.of(profileImageForm.getFileId()));
        } catch (IllegalArgumentException | ProfileImageStorageException e) {
            bindingResult.rejectValue("fileId", "profileImageForm.fileId.invalid");
            populateModel(model);
            return "profile/profile";
        }

        return "redirect:/profile?updated=true";
    }

    private void populateModel(Model model) {
        var userProfile = userProfileService.getCurrentUser();

        model.addAttribute("currentUser", new CurrentUserProfileDto(
            userProfile.getId().toString(),
            userProfile.getFullName(),
            userProfile.getLogin().toString(),
            profileImageService.currentUserHasProfileImage()));
    }
}
