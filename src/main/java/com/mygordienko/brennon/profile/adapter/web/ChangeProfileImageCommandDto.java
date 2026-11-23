package com.mygordienko.brennon.profile.adapter.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChangeProfileImageCommandDto {
    public ChangeProfileImageCommandDto() {
    }

    public ChangeProfileImageCommandDto(Integer version, String fileId) {
        this.version = version;
        this.fileId = fileId;
    }

    @NotNull
    private Integer version;

    @NotBlank
    private String fileId;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
