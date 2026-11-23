package com.mygordienko.brennon.profile;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.common.BrennonDomainEvent;
import com.mygordienko.brennon.core.common.FileId;

public record ProfileImageUpdatedEvent(
        BrennonUserId profileId,
        FileId previousFileId,
        FileId newFileId) implements BrennonDomainEvent {
    @Override
    public String toString() {
        return "profile image updated: %s:%s:%s".formatted(profileId, previousFileId, newFileId);
    }
}
