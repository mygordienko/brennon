package com.mygordienko.brennon.profile.adapter.web;

import jakarta.validation.constraints.NotBlank;

public class ChangeProfileImageCommandDto {
    public ChangeProfileImageCommandDto() {
    }

    public ChangeProfileImageCommandDto(String fileId) {
        this.fileId = fileId;
    }

    @NotBlank
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
