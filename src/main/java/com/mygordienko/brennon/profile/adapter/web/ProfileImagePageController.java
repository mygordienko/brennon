package com.mygordienko.brennon.profile.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.mygordienko.brennon.core.common.FileId;
import com.mygordienko.brennon.profile.BrennonUserProfile;
import com.mygordienko.brennon.profile.ProfileImageStorageException;
import com.mygordienko.brennon.profile.adapter.rest.ProfileImageUrlResolver;
import com.mygordienko.brennon.profile.internal.BrennonUserProfileService;
import com.mygordienko.brennon.profile.internal.ProfileImageService;

@Controller
public class ProfileImagePageController {
    public ProfileImagePageController(
            ProfileImageService profileImageService,
            BrennonUserProfileService userProfileService,
            ProfileImageUrlResolver profileImageUrlResolver) {
        this.profileImageService = profileImageService;
        this.userProfileService = userProfileService;
        this.profileImageUrlResolver = profileImageUrlResolver;
    }

    private final ProfileImageService profileImageService;
    private final BrennonUserProfileService userProfileService;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    @GetMapping("/profile")
    public String getProfile(Model model) {
        var userProfile = userProfileService.getCurrentUser();
        populateModel(model, userProfile);
        model.addAttribute("profileImageForm", new ChangeProfileImageCommandDto(userProfile.getVersion(), null));
        return "profile/profile";
    }

    @PostMapping("/profile/image")
    public String saveProfileImage(
            @Validated @ModelAttribute("profileImageForm") ChangeProfileImageCommandDto profileImageForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            profileImageForm.setVersion(populateModel(model).getVersion());
            return "profile/profile";
        }

        try {
            profileImageService.saveProfileImage(FileId.of(profileImageForm.getFileId()), profileImageForm.getVersion());
        } catch (IllegalArgumentException | ProfileImageStorageException e) {
            bindingResult.rejectValue("fileId", "profileImageForm.fileId.invalid");
            profileImageForm.setVersion(populateModel(model).getVersion());
            return "profile/profile";
        }

        return "redirect:/profile?updated=true";
    }

    private BrennonUserProfile populateModel(Model model) {
        var userProfile = userProfileService.getCurrentUser();
        populateModel(model, userProfile);
        return userProfile;
    }

    private void populateModel(Model model, BrennonUserProfile userProfile) {
        model.addAttribute("currentUser", new CurrentUserProfileDto(
            userProfile.getId().toString(),
            userProfile.getFullName(),
            userProfile.getLogin().toString(),
            profileImageUrlResolver.resolve(userProfile).orElse(null)));
    }
}
